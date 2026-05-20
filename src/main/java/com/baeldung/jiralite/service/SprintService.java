package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.Sprint;
import com.baeldung.jiralite.domain.enums.SprintStatus;
import com.baeldung.jiralite.dto.SprintRequest;
import com.baeldung.jiralite.dto.SprintResponse;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import com.baeldung.jiralite.repository.SprintRepository;
import com.baeldung.jiralite.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectService projectService;
    private final SecurityUtils securityUtils;
    private final AuditLogService auditLogService;

    public SprintService(SprintRepository sprintRepository, ProjectService projectService,
                         SecurityUtils securityUtils, AuditLogService auditLogService) {
        this.sprintRepository = sprintRepository;
        this.projectService = projectService;
        this.securityUtils = securityUtils;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public SprintResponse createSprint(Long projectId, SprintRequest request) {
        var project = projectService.findProjectWithAccess(projectId);
        var actor = securityUtils.getCurrentUser();
        projectService.requireManagerOrAdmin(actor, projectId);
        Sprint sprint = Sprint.builder()
                .project(project)
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        return SprintResponse.from(sprintRepository.save(sprint));
    }

    @Transactional(readOnly = true)
    public List<SprintResponse> listSprints(Long projectId) {
        projectService.findProjectWithAccess(projectId);
        return sprintRepository.findByProjectId(projectId).stream()
                .map(SprintResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SprintResponse getSprint(Long projectId, Long sprintId) {
        projectService.findProjectWithAccess(projectId);
        return SprintResponse.from(findSprint(projectId, sprintId));
    }

    @Transactional
    public SprintResponse updateSprint(Long projectId, Long sprintId, SprintRequest request) {
        projectService.findProjectWithAccess(projectId);
        var actor = securityUtils.getCurrentUser();
        projectService.requireManagerOrAdmin(actor, projectId);
        Sprint sprint = findSprint(projectId, sprintId);
        sprint.setName(request.getName());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        return SprintResponse.from(sprintRepository.save(sprint));
    }

    @Transactional
    public SprintResponse startSprint(Long projectId, Long sprintId) {
        projectService.findProjectWithAccess(projectId);
        var actor = securityUtils.getCurrentUser();
        projectService.requireManagerOrAdmin(actor, projectId);
        Sprint sprint = findSprint(projectId, sprintId);
        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new IllegalArgumentException("Sprint can only be started from PLANNED status");
        }
        sprint.setStatus(SprintStatus.ACTIVE);
        sprint = sprintRepository.save(sprint);
        auditLogService.log("SPRINT_STARTED", actor.getId(), projectId, null,
                "Sprint started: " + sprint.getName());
        return SprintResponse.from(sprint);
    }

    @Transactional
    public SprintResponse completeSprint(Long projectId, Long sprintId) {
        projectService.findProjectWithAccess(projectId);
        var actor = securityUtils.getCurrentUser();
        projectService.requireManagerOrAdmin(actor, projectId);
        Sprint sprint = findSprint(projectId, sprintId);
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new IllegalArgumentException("Sprint can only be completed from ACTIVE status");
        }
        sprint.setStatus(SprintStatus.COMPLETED);
        sprint = sprintRepository.save(sprint);
        auditLogService.log("SPRINT_COMPLETED", actor.getId(), projectId, null,
                "Sprint completed: " + sprint.getName());
        return SprintResponse.from(sprint);
    }

    private Sprint findSprint(Long projectId, Long sprintId) {
        return sprintRepository.findByIdAndProjectId(sprintId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sprint not found: " + sprintId + " in project " + projectId));
    }
}
