package com.baeldung.jiralite.sprint;

import com.baeldung.jiralite.audit.AuditEntityType;
import com.baeldung.jiralite.audit.AuditEventType;
import com.baeldung.jiralite.audit.AuditLogger;
import com.baeldung.jiralite.audit.AuditWrite;
import com.baeldung.jiralite.project.Project;
import com.baeldung.jiralite.project.ProjectService;
import com.baeldung.jiralite.user.Role;
import com.baeldung.jiralite.web.ConflictException;
import com.baeldung.jiralite.web.ForbiddenException;
import com.baeldung.jiralite.web.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectService projectService;
    private final SprintMapper sprintMapper;
    private final AuditLogger audit;

    public SprintService(SprintRepository sprintRepository, ProjectService projectService, SprintMapper sprintMapper,
                         AuditLogger audit) {
        this.sprintRepository = sprintRepository;
        this.projectService = projectService;
        this.sprintMapper = sprintMapper;
        this.audit = audit;
    }

    @Transactional
    public SprintResponse createSprint(CreateSprintRequest request) {
        Project project = loadManagedProject(request.projectId());
        Sprint sprint = sprintRepository.save(new Sprint(project, request.name(), request.startDate(), request.endDate()));
        audit.log(new AuditWrite(AuditEventType.SPRINT_CREATED, AuditEntityType.SPRINT, sprint.getId(), project.getId(), null));
        return sprintMapper.toResponse(sprint);
    }

    @Transactional
    public SprintResponse start(Long sprintId) {
        Sprint sprint = loadManagedSprint(sprintId);
        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new ConflictException("Sprint is not in PLANNED state");
        }
        sprint.setStatus(SprintStatus.ACTIVE);
        audit.log(new AuditWrite(AuditEventType.SPRINT_STARTED, AuditEntityType.SPRINT, sprint.getId(),
                sprint.getProject().getId(), null));
        return sprintMapper.toResponse(sprint);
    }

    @Transactional
    public SprintResponse complete(Long sprintId) {
        Sprint sprint = loadManagedSprint(sprintId);
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new ConflictException("Sprint is not in ACTIVE state");
        }
        sprint.setStatus(SprintStatus.COMPLETED);
        audit.log(new AuditWrite(AuditEventType.SPRINT_COMPLETED, AuditEntityType.SPRINT, sprint.getId(),
                sprint.getProject().getId(), null));
        return sprintMapper.toResponse(sprint);
    }

    @Transactional(readOnly = true)
    public List<SprintResponse> listForProject(Long projectId) {
        projectService.requireVisible(projectId);
        return sprintRepository.findByProjectId(projectId).stream().map(sprintMapper::toResponse).toList();
    }

    public Sprint loadVisibleSprint(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> sprintNotFound(sprintId));
        try {
            projectService.requireVisible(sprint.getProject().getId());
        } catch (NotFoundException e) {
            throw sprintNotFound(sprintId);
        }
        return sprint;
    }

    private Project loadManagedProject(Long projectId) {
        Project project = projectService.loadVisibleProject(projectId);
        requireManagerOrAdmin();
        return project;
    }

    private Sprint loadManagedSprint(Long sprintId) {
        Sprint sprint = loadVisibleSprint(sprintId);
        requireManagerOrAdmin();
        return sprint;
    }

    private void requireManagerOrAdmin() {
        Role role = audit.currentRole();
        if (role != Role.ADMIN && role != Role.MANAGER) {
            throw new ForbiddenException("Manager or Admin role required");
        }
    }

    private static NotFoundException sprintNotFound(Long sprintId) {
        return new NotFoundException("Sprint " + sprintId + " not found");
    }
}
