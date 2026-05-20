package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.AuditEventType;
import com.baeldung.jiralite.domain.Project;
import com.baeldung.jiralite.domain.Sprint;
import com.baeldung.jiralite.domain.SprintStatus;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.dto.SprintRequest;
import com.baeldung.jiralite.dto.SprintResponse;
import com.baeldung.jiralite.exception.InvalidTransitionException;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import com.baeldung.jiralite.repository.ProjectRepository;
import com.baeldung.jiralite.repository.SprintRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final AuditLogService auditLogService;

    public SprintService(SprintRepository sprintRepository, ProjectRepository projectRepository,
                         AuditLogService auditLogService) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public SprintResponse createSprint(Long projectId, SprintRequest req, User actor) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        Sprint sprint = new Sprint(project, req.name(), req.startDate(), req.endDate());
        sprintRepository.save(sprint);
        auditLogService.log(AuditEventType.SPRINT_CREATED, actor, "SPRINT", sprint.getId(), project);
        return SprintResponse.from(sprint);
    }

    @Transactional
    public SprintResponse startSprint(Long sprintId, User actor) {
        Sprint sprint = findSprint(sprintId);
        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new InvalidTransitionException(
                "Sprint can only be started from PLANNED state, current: " + sprint.getStatus());
        }
        sprint.setStatus(SprintStatus.ACTIVE);
        sprintRepository.save(sprint);
        auditLogService.log(AuditEventType.SPRINT_STARTED, actor, "SPRINT", sprintId, sprint.getProject());
        return SprintResponse.from(sprint);
    }

    @Transactional
    public SprintResponse completeSprint(Long sprintId, User actor) {
        Sprint sprint = findSprint(sprintId);
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new InvalidTransitionException(
                "Sprint can only be completed from ACTIVE state, current: " + sprint.getStatus());
        }
        sprint.setStatus(SprintStatus.COMPLETED);
        sprintRepository.save(sprint);
        auditLogService.log(AuditEventType.SPRINT_COMPLETED, actor, "SPRINT", sprintId, sprint.getProject());
        return SprintResponse.from(sprint);
    }

    private Sprint findSprint(Long sprintId) {
        return sprintRepository.findById(sprintId)
            .orElseThrow(() -> new ResourceNotFoundException("Sprint not found: " + sprintId));
    }
}
