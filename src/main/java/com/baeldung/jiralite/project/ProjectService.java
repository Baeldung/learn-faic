package com.baeldung.jiralite.project;

import com.baeldung.jiralite.audit.AuditEntityType;
import com.baeldung.jiralite.audit.AuditEventType;
import com.baeldung.jiralite.audit.AuditLogger;
import com.baeldung.jiralite.audit.AuditWrite;
import com.baeldung.jiralite.user.Role;
import com.baeldung.jiralite.user.User;
import com.baeldung.jiralite.user.UserRepository;
import com.baeldung.jiralite.web.NotFoundException;
import com.baeldung.jiralite.web.ValidationException;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final AuditLogger audit;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, ProjectMapper projectMapper,
                          AuditLogger audit) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMapper = projectMapper;
        this.audit = audit;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        Project project = new Project(request.name(), request.description());
        User actor = audit.currentEntity();
        project.getMembers().add(actor);
        Project saved = projectRepository.save(project);
        audit.log(new AuditWrite(AuditEventType.PROJECT_CREATED, AuditEntityType.PROJECT, saved.getId(), saved.getId(), null));
        return projectMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects() {
        List<Project> projects = audit.isAdmin()
                ? projectRepository.findAllWithMembers()
                : projectRepository.findAllForMember(audit.currentId());
        return projects.stream().map(projectMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long projectId) {
        return projectMapper.toResponse(loadVisibleProject(projectId));
    }

    @Transactional
    public ProjectResponse addMember(Long projectId, Long userId) {
        Project project = loadProjectForManagement(projectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
        project.getMembers().add(user);
        audit.log(new AuditWrite(AuditEventType.MEMBER_ADDED, AuditEntityType.PROJECT, project.getId(), project.getId(),
                "userId=" + userId));
        return projectMapper.toResponse(project);
    }

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        Project project = loadProjectForManagement(projectId);
        boolean removed = project.getMembers().removeIf(u -> u.getId().equals(userId));
        if (!removed) {
            throw new NotFoundException("User " + userId + " not a member");
        }
        audit.log(new AuditWrite(AuditEventType.MEMBER_REMOVED, AuditEntityType.PROJECT, project.getId(), project.getId(),
                "userId=" + userId));
    }

    @Transactional(readOnly = true)
    public Project loadVisibleProject(Long projectId) {
        Project project = projectRepository.findByIdWithMembers(projectId)
                .orElseThrow(() -> notFound(projectId));
        if (!audit.isAdmin() && !project.hasMember(audit.currentId())) {
            throw notFound(projectId);
        }
        return project;
    }

    @Transactional(readOnly = true)
    public void requireVisible(Long projectId) {
        loadVisibleProject(projectId);
    }

    @Transactional(readOnly = true)
    public void requireMember(Long projectId, Long userId) {
        Project project = projectRepository.findByIdWithMembers(projectId)
                .orElseThrow(() -> notFound(projectId));
        if (!project.hasMember(userId)) {
            throw new ValidationException("User " + userId + " is not a member of project " + projectId);
        }
    }

    @Transactional(readOnly = true)
    public List<Long> visibleProjectIds() {
        if (audit.isAdmin()) {
            return projectRepository.findAllWithMembers().stream().map(Project::getId).toList();
        }
        return projectRepository.findAllForMember(audit.currentId()).stream().map(Project::getId).toList();
    }

    private Project loadProjectForManagement(Long projectId) {
        Project project = projectRepository.findByIdWithMembers(projectId)
                .orElseThrow(() -> notFound(projectId));
        if (audit.isAdmin()) {
            return project;
        }
        if (audit.currentRole() != Role.MANAGER || !project.hasMember(audit.currentId())) {
            throw notFound(projectId);
        }
        return project;
    }

    private static NotFoundException notFound(Long projectId) {
        return new NotFoundException("Project " + projectId + " not found");
    }
}
