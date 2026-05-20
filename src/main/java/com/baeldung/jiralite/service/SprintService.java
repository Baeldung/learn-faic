package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.AuditEventType;
import com.baeldung.jiralite.domain.Project;
import com.baeldung.jiralite.domain.Sprint;
import com.baeldung.jiralite.domain.SprintStatus;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.dto.SprintRequest;
import com.baeldung.jiralite.dto.SprintResponse;
import com.baeldung.jiralite.exception.ConflictException;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import com.baeldung.jiralite.repository.ProjectRepository;
import com.baeldung.jiralite.repository.SprintRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;

    private final ProjectRepository projectRepository;

    private final CurrentUserService currentUserService;

    private final AuditLogService auditLogService;

    public SprintService(SprintRepository sprintRepository,
            ProjectRepository projectRepository,
            CurrentUserService currentUserService,
            AuditLogService auditLogService) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public SprintResponse createSprint(Long projectId, SprintRequest request) {
        User caller = currentUserService.getCurrentUser();
        Project project = findProject(projectId);
        Sprint sprint = new Sprint();
        sprint.setProject(project);
        sprint.setName(request.getName());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint.setStatus(SprintStatus.PLANNED);
        sprintRepository.save(sprint);
        auditLogService.record(AuditEventType.SPRINT_CREATED, caller, projectId, null,
            "Sprint created: " + sprint.getName());
        return toResponse(sprint);
    }

    @Transactional
    public SprintResponse startSprint(Long sprintId) {
        User caller = currentUserService.getCurrentUser();
        Sprint sprint = findSprint(sprintId);
        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new ConflictException("Sprint can only be started from PLANNED status");
        }
        sprint.setStatus(SprintStatus.ACTIVE);
        sprintRepository.save(sprint);
        auditLogService.record(AuditEventType.SPRINT_STARTED, caller, sprint.getProject().getId(), null,
            "Sprint started: " + sprint.getName());
        return toResponse(sprint);
    }

    @Transactional
    public SprintResponse completeSprint(Long sprintId) {
        User caller = currentUserService.getCurrentUser();
        Sprint sprint = findSprint(sprintId);
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new ConflictException("Sprint can only be completed from ACTIVE status");
        }
        sprint.setStatus(SprintStatus.COMPLETED);
        sprintRepository.save(sprint);
        auditLogService.record(AuditEventType.SPRINT_COMPLETED, caller, sprint.getProject().getId(), null,
            "Sprint completed: " + sprint.getName());
        return toResponse(sprint);
    }

    private Sprint findSprint(Long sprintId) {
        return sprintRepository.findById(sprintId)
            .orElseThrow(() -> new ResourceNotFoundException("Sprint not found: " + sprintId));
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }

    private SprintResponse toResponse(Sprint sprint) {
        SprintResponse resp = new SprintResponse();
        resp.setId(sprint.getId());
        resp.setProjectId(sprint.getProject().getId());
        resp.setName(sprint.getName());
        resp.setStartDate(sprint.getStartDate());
        resp.setEndDate(sprint.getEndDate());
        resp.setStatus(sprint.getStatus());
        return resp;
    }
}
