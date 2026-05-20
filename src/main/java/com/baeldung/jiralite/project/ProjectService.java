package com.baeldung.jiralite.project;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baeldung.jiralite.audit.AuditEventType;
import com.baeldung.jiralite.audit.AuditService;
import com.baeldung.jiralite.security.CurrentUser;
import com.baeldung.jiralite.user.Role;
import com.baeldung.jiralite.user.User;
import com.baeldung.jiralite.user.UserRepository;
import com.baeldung.jiralite.web.ForbiddenException;
import com.baeldung.jiralite.web.NotFoundException;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final ProjectMapper projectMapper;

    private final AuditService auditService;

    private final CurrentUser currentUser;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
            ProjectMapper projectMapper, AuditService auditService, CurrentUser currentUser) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMapper = projectMapper;
        this.auditService = auditService;
        this.currentUser = currentUser;
    }

    public ProjectResponse createProject(CreateProjectRequest request) {
        User actor = currentUser.get();
        Project project = new Project(request.name(), request.description());
        project.getMembers().add(actor);
        Project saved = projectRepository.save(project);
        auditService.record(AuditEventType.PROJECT_CREATED, actor, saved.getId(), null);
        return projectMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects() {
        return projectRepository.findAll().stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long projectId) {
        Project project = findEntity(projectId);
        requireMemberOrAdmin(project, currentUser.getId());
        return projectMapper.toResponse(project);
    }

    public ProjectResponse addMember(Long projectId, AddMemberRequest request) {
        User actor = currentUser.get();
        Project project = findEntity(projectId);
        requireMemberOrAdmin(project, actor.getId());
        User newMember = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("User " + request.userId() + " not found"));
        project.getMembers().add(newMember);
        auditService.record(AuditEventType.PROJECT_MEMBER_ADDED, actor, projectId, null);
        return projectMapper.toResponse(project);
    }

    public ProjectResponse removeMember(Long projectId, Long userId) {
        User actor = currentUser.get();
        Project project = findEntity(projectId);
        requireMemberOrAdmin(project, actor.getId());
        User member = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
        project.getMembers().remove(member);
        auditService.record(AuditEventType.PROJECT_MEMBER_REMOVED, actor, projectId, null);
        return projectMapper.toResponse(project);
    }

    public boolean isMember(Long projectId, Long userId) {
        return projectRepository.isMember(projectId, userId);
    }

    public Project findEntity(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project " + projectId + " not found"));
    }

    public void requireMemberOrAdmin(Project project, Long userId) {
        boolean isAdmin = project.getMembers().stream()
                .noneMatch(m -> m.getId().equals(userId));
        if (isAdmin) {
            User user = currentUser.get();
            if (user.getRole() != Role.ADMIN) {
                throw new ForbiddenException("Not a member of this project");
            }
        }
    }

    public void requireMemberOrAdminById(Long projectId, Long userId) {
        if (!projectRepository.isMember(projectId, userId)) {
            User user = currentUser.get();
            if (user.getRole() != Role.ADMIN) {
                throw new ForbiddenException("Not a member of this project");
            }
        }
    }
}
