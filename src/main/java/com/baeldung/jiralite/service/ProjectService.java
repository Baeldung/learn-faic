package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.Project;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.domain.enums.Role;
import com.baeldung.jiralite.dto.AddMemberRequest;
import com.baeldung.jiralite.dto.ProjectRequest;
import com.baeldung.jiralite.dto.ProjectResponse;
import com.baeldung.jiralite.exception.ForbiddenException;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import com.baeldung.jiralite.repository.ProjectRepository;
import com.baeldung.jiralite.repository.UserRepository;
import com.baeldung.jiralite.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final AuditLogService auditLogService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
                          SecurityUtils securityUtils, AuditLogService auditLogService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        User actor = securityUtils.getCurrentUser();
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(actor)
                .build();
        project.getMembers().add(actor);
        project = projectRepository.save(project);
        return ProjectResponse.from(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects() {
        User actor = securityUtils.getCurrentUser();
        List<Project> projects;
        if (actor.getRole() == Role.ADMIN) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findByMember(actor);
        }
        return projects.stream().map(ProjectResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long projectId) {
        Project project = findProjectWithAccess(projectId);
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest request) {
        Project project = findProjectWithAccess(projectId);
        User actor = securityUtils.getCurrentUser();
        requireManagerOrAdmin(actor, projectId);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional
    public void addMember(Long projectId, AddMemberRequest request) {
        User actor = securityUtils.getCurrentUser();
        requireManagerOrAdmin(actor, projectId);
        Project project = projectRepository.findByIdWithMembers(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        User newMember = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));
        project.getMembers().add(newMember);
        projectRepository.save(project);
        auditLogService.log("PROJECT_MEMBER_ADDED", actor.getId(), projectId, null,
                "Added user " + newMember.getUsername());
    }

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        User actor = securityUtils.getCurrentUser();
        requireManagerOrAdmin(actor, projectId);
        Project project = projectRepository.findByIdWithMembers(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        User member = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        project.getMembers().remove(member);
        projectRepository.save(project);
        auditLogService.log("PROJECT_MEMBER_REMOVED", actor.getId(), projectId, null,
                "Removed user " + member.getUsername());
    }

    public Project findProjectWithAccess(Long projectId) {
        User actor = securityUtils.getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        if (actor.getRole() != Role.ADMIN && !projectRepository.isMember(projectId, actor.getId())) {
            throw new ForbiddenException("You are not a member of this project");
        }
        return project;
    }

    public void requireManagerOrAdmin(User user, Long projectId) {
        if (user.getRole() == Role.ADMIN) return;
        if (user.getRole() == Role.MANAGER && projectRepository.isMember(projectId, user.getId())) return;
        throw new ForbiddenException("Only ADMIN or project MANAGER can perform this action");
    }
}
