package com.baeldung.jiralite.sprint;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baeldung.jiralite.audit.AuditEventType;
import com.baeldung.jiralite.audit.AuditService;
import com.baeldung.jiralite.project.Project;
import com.baeldung.jiralite.project.ProjectService;
import com.baeldung.jiralite.security.CurrentUser;
import com.baeldung.jiralite.user.User;
import com.baeldung.jiralite.web.ConflictException;
import com.baeldung.jiralite.web.NotFoundException;

@Service
@Transactional
public class SprintService {

    private final SprintRepository sprintRepository;

    private final ProjectService projectService;

    private final SprintMapper sprintMapper;

    private final AuditService auditService;

    private final CurrentUser currentUser;

    public SprintService(SprintRepository sprintRepository, ProjectService projectService,
            SprintMapper sprintMapper, AuditService auditService, CurrentUser currentUser) {
        this.sprintRepository = sprintRepository;
        this.projectService = projectService;
        this.sprintMapper = sprintMapper;
        this.auditService = auditService;
        this.currentUser = currentUser;
    }

    public SprintResponse createSprint(CreateSprintRequest request) {
        User actor = currentUser.get();
        Project project = projectService.findEntity(request.projectId());
        projectService.requireMemberOrAdmin(project, actor.getId());
        Sprint sprint = new Sprint(project, request.name(), request.startDate(), request.endDate());
        Sprint saved = sprintRepository.save(sprint);
        auditService.record(AuditEventType.SPRINT_CREATED, actor, project.getId(), null);
        return sprintMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SprintResponse> listSprints(Long projectId) {
        User actor = currentUser.get();
        Project project = projectService.findEntity(projectId);
        projectService.requireMemberOrAdmin(project, actor.getId());
        return sprintRepository.findByProjectId(projectId).stream()
                .map(sprintMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SprintResponse getSprint(Long sprintId) {
        Sprint sprint = findEntity(sprintId);
        User actor = currentUser.get();
        Project project = sprint.getProject();
        projectService.requireMemberOrAdmin(project, actor.getId());
        return sprintMapper.toResponse(sprint);
    }

    public SprintResponse startSprint(Long sprintId) {
        User actor = currentUser.get();
        Sprint sprint = findEntity(sprintId);
        projectService.requireMemberOrAdmin(sprint.getProject(), actor.getId());
        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new ConflictException("Sprint can only be started from PLANNED state");
        }
        sprint.setStatus(SprintStatus.ACTIVE);
        auditService.record(AuditEventType.SPRINT_STARTED, actor, sprint.getProject().getId(), null);
        return sprintMapper.toResponse(sprint);
    }

    public SprintResponse completeSprint(Long sprintId) {
        User actor = currentUser.get();
        Sprint sprint = findEntity(sprintId);
        projectService.requireMemberOrAdmin(sprint.getProject(), actor.getId());
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new ConflictException("Sprint can only be completed from ACTIVE state");
        }
        sprint.setStatus(SprintStatus.COMPLETED);
        auditService.record(AuditEventType.SPRINT_COMPLETED, actor, sprint.getProject().getId(), null);
        return sprintMapper.toResponse(sprint);
    }

    public Sprint findEntity(Long sprintId) {
        return sprintRepository.findById(sprintId)
                .orElseThrow(() -> new NotFoundException("Sprint " + sprintId + " not found"));
    }
}
