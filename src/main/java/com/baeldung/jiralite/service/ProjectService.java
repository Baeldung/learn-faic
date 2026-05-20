package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.AuditEventType;
import com.baeldung.jiralite.domain.Project;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.dto.ProjectRequest;
import com.baeldung.jiralite.dto.ProjectResponse;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import com.baeldung.jiralite.repository.ProjectRepository;
import com.baeldung.jiralite.repository.UserRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final CurrentUserService currentUserService;

    private final AuditLogService auditLogService;

    public ProjectService(ProjectRepository projectRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            AuditLogService auditLogService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        User caller = currentUserService.getCurrentUser();
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        projectRepository.save(project);
        caller.getProjects().add(project);
        userRepository.save(caller);
        auditLogService.record(AuditEventType.PROJECT_CREATED, caller, project.getId(), null,
            "Project created: " + project.getName());
        return toResponse(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects() {
        return projectRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponse addMember(Long projectId, Long userId) {
        User caller = currentUserService.getCurrentUser();
        Project project = findProject(projectId);
        User newMember = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        newMember.getProjects().add(project);
        userRepository.save(newMember);
        auditLogService.record(AuditEventType.MEMBER_ADDED, caller, projectId, null,
            "User " + newMember.getUsername() + " added to project " + project.getName());
        return toResponse(project);
    }

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        User caller = currentUserService.getCurrentUser();
        Project project = findProject(projectId);
        User member = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        member.getProjects().remove(project);
        userRepository.save(member);
        auditLogService.record(AuditEventType.MEMBER_REMOVED, caller, projectId, null,
            "User " + member.getUsername() + " removed from project " + project.getName());
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }

    private ProjectResponse toResponse(Project project) {
        Set<Long> memberIds = project.getMembers().stream()
            .map(User::getId)
            .collect(Collectors.toSet());
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), memberIds);
    }
}
