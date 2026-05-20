package com.baeldung.jiralite2.service;

import com.baeldung.jiralite2.domain.Project;
import com.baeldung.jiralite2.domain.User;
import com.baeldung.jiralite2.domain.enums.AuditEventType;
import com.baeldung.jiralite2.dto.request.AddMemberRequest;
import com.baeldung.jiralite2.dto.request.CreateProjectRequest;
import com.baeldung.jiralite2.dto.response.ProjectResponse;
import com.baeldung.jiralite2.exception.ResourceNotFoundException;
import com.baeldung.jiralite2.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final AuditLogService auditLogService;

    public ProjectService(ProjectRepository projectRepository, UserService userService, AuditLogService auditLogService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ProjectResponse create(CreateProjectRequest req, String actorUsername) {
        User actor = userService.loadByUsername(actorUsername);
        Project project = new Project(req.name(), req.description());
        project.getMembers().add(actor);
        projectRepository.save(project);
        auditLogService.record(AuditEventType.PROJECT_CREATED, actor, project, null, "Project created: " + project.getName());
        return ProjectResponse.from(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listAll() {
        return projectRepository.findAll().stream().map(ProjectResponse::from).toList();
    }

    @Transactional
    public ProjectResponse addMember(Long projectId, AddMemberRequest req, String actorUsername) {
        User actor = userService.loadByUsername(actorUsername);
        Project project = loadById(projectId);
        User newMember = userService.loadById(req.userId());
        project.getMembers().add(newMember);
        projectRepository.save(project);
        auditLogService.record(AuditEventType.MEMBER_ADDED, actor, project, null,
            newMember.getUsername() + " added to project");
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse removeMember(Long projectId, Long userId, String actorUsername) {
        User actor = userService.loadByUsername(actorUsername);
        Project project = loadById(projectId);
        User member = userService.loadById(userId);
        project.getMembers().remove(member);
        projectRepository.save(project);
        auditLogService.record(AuditEventType.MEMBER_REMOVED, actor, project, null,
            member.getUsername() + " removed from project");
        return ProjectResponse.from(project);
    }

    public Project loadById(Long id) {
        return projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }
}
