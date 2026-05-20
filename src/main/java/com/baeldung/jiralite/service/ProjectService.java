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
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private static final String ENTITY_PROJECT = "PROJECT";
    private static final String PROJECT_NOT_FOUND = "Project not found";

    @Autowired
    private ProjectRepository projectRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public ProjectResponse createProject(ProjectRequest request, User actor) {
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.getMembers().add(actor);
        projectRepo.save(project);
        auditLogService.log(AuditEventType.PROJECT_CREATED, actor, ENTITY_PROJECT, project.getId(), project);
        return toResponse(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects() {
        return projectRepo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ProjectResponse addMember(Long projectId, AddMemberRequest request, User actor) {
        Project project = findProject(projectId);
        User user = userRepo.findById(request.userId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        project.getMembers().add(user);
        projectRepo.save(project);
        auditLogService.log(AuditEventType.MEMBER_ADDED, actor, ENTITY_PROJECT, projectId, project);
        return toResponse(project);
    }

    @Transactional
    public void removeMember(Long projectId, Long userId, User actor) {
        Project project = findProject(projectId);
        project.getMembers().removeIf(m -> m.getId().equals(userId));
        projectRepo.save(project);
        auditLogService.log(AuditEventType.MEMBER_REMOVED, actor, ENTITY_PROJECT, projectId, project);
    }

    private Project findProject(Long projectId) {
        return projectRepo.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND));
    }

    private ProjectResponse toResponse(Project project) {
        List<Long> memberIds = project.getMembers().stream().map(User::getId).toList();
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), memberIds);
    }
}
