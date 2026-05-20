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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SprintService {

    private static final String ENTITY_SPRINT = "SPRINT";
    private static final String SPRINT_NOT_FOUND = "Sprint not found";

    @Autowired
    private SprintRepository sprintRepo;

    @Autowired
    private ProjectRepository projectRepo;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public SprintResponse createSprint(Long projectId, SprintRequest request, User actor) {
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        Sprint sprint = new Sprint();
        sprint.setProject(project);
        sprint.setName(request.name());
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprintRepo.save(sprint);
        auditLogService.log(AuditEventType.SPRINT_CREATED, actor, ENTITY_SPRINT, sprint.getId(), project);
        return toResponse(sprint);
    }

    @Transactional
    public SprintResponse startSprint(Long sprintId, User actor) {
        Sprint sprint = findSprint(sprintId);
        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new ConflictException("Sprint must be PLANNED to start");
        }
        sprint.setStatus(SprintStatus.ACTIVE);
        sprintRepo.save(sprint);
        auditLogService.log(AuditEventType.SPRINT_STARTED, actor, ENTITY_SPRINT, sprintId, sprint.getProject());
        return toResponse(sprint);
    }

    @Transactional
    public SprintResponse completeSprint(Long sprintId, User actor) {
        Sprint sprint = findSprint(sprintId);
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new ConflictException("Sprint must be ACTIVE to complete");
        }
        sprint.setStatus(SprintStatus.COMPLETED);
        sprintRepo.save(sprint);
        auditLogService.log(AuditEventType.SPRINT_COMPLETED, actor, ENTITY_SPRINT, sprintId, sprint.getProject());
        return toResponse(sprint);
    }

    private Sprint findSprint(Long sprintId) {
        return sprintRepo.findById(sprintId)
            .orElseThrow(() -> new ResourceNotFoundException(SPRINT_NOT_FOUND));
    }

    private SprintResponse toResponse(Sprint sprint) {
        SprintResponse r = new SprintResponse();
        r.setId(sprint.getId());
        r.setProjectId(sprint.getProject().getId());
        r.setName(sprint.getName());
        r.setStartDate(sprint.getStartDate());
        r.setEndDate(sprint.getEndDate());
        r.setStatus(sprint.getStatus());
        return r;
    }
}
