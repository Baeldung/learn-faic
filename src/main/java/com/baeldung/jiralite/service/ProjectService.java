package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.AuditEventType;
import com.baeldung.jiralite.domain.Project;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.dto.AddMemberRequest;
import com.baeldung.jiralite.dto.ProjectRequest;
import com.baeldung.jiralite.dto.ProjectResponse;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import com.baeldung.jiralite.repository.ProjectRepository;
import com.baeldung.jiralite.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
                          AuditLogService auditLogService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest req, User actor) {
        Project project = new Project(req.name(), req.description());
        project.getMembers().add(actor);
        projectRepository.save(project);
        auditLogService.log(AuditEventType.PROJECT_CREATED, actor, "PROJECT", project.getId(), project);
        return ProjectResponse.from(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects() {
        return projectRepository.findAll().stream()
            .map(ProjectResponse::from)
            .toList();
    }

    @Transactional
    public ProjectResponse addMember(Long projectId, AddMemberRequest req, User actor) {
        Project project = findProject(projectId);
        User user = userRepository.findById(req.userId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + req.userId()));
        project.getMembers().add(user);
        projectRepository.save(project);
        auditLogService.log(AuditEventType.MEMBER_ADDED, actor, "USER", req.userId(), project);
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse removeMember(Long projectId, Long userId, User actor) {
        Project project = findProject(projectId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        project.getMembers().remove(user);
        projectRepository.save(project);
        auditLogService.log(AuditEventType.MEMBER_REMOVED, actor, "USER", userId, project);
        return ProjectResponse.from(project);
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }
}
