package com.baeldung.jiralite2.service;

import com.baeldung.jiralite2.domain.Project;
import com.baeldung.jiralite2.domain.Sprint;
import com.baeldung.jiralite2.domain.User;
import com.baeldung.jiralite2.domain.enums.AuditEventType;
import com.baeldung.jiralite2.domain.enums.SprintStatus;
import com.baeldung.jiralite2.dto.request.CreateSprintRequest;
import com.baeldung.jiralite2.dto.response.SprintResponse;
import com.baeldung.jiralite2.exception.InvalidTransitionException;
import com.baeldung.jiralite2.exception.ResourceNotFoundException;
import com.baeldung.jiralite2.repository.SprintRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final AuditLogService auditLogService;

    public SprintService(SprintRepository sprintRepository, ProjectService projectService,
                         UserService userService, AuditLogService auditLogService) {
        this.sprintRepository = sprintRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public SprintResponse create(Long projectId, CreateSprintRequest req, String actorUsername) {
        User actor = userService.loadByUsername(actorUsername);
        Project project = projectService.loadById(projectId);
        Sprint sprint = new Sprint(project, req.name(), req.startDate(), req.endDate());
        sprintRepository.save(sprint);
        auditLogService.record(AuditEventType.SPRINT_CREATED, actor, project, null,
            "Sprint created: " + sprint.getName());
        return SprintResponse.from(sprint);
    }

    @Transactional
    public SprintResponse start(Long sprintId, String actorUsername) {
        User actor = userService.loadByUsername(actorUsername);
        Sprint sprint = loadById(sprintId);
        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new InvalidTransitionException("Sprint must be PLANNED to start");
        }
        sprint.setStatus(SprintStatus.ACTIVE);
        sprintRepository.save(sprint);
        auditLogService.record(AuditEventType.SPRINT_STARTED, actor, sprint.getProject(), null,
            "Sprint started: " + sprint.getName());
        return SprintResponse.from(sprint);
    }

    @Transactional
    public SprintResponse complete(Long sprintId, String actorUsername) {
        User actor = userService.loadByUsername(actorUsername);
        Sprint sprint = loadById(sprintId);
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new InvalidTransitionException("Sprint must be ACTIVE to complete");
        }
        sprint.setStatus(SprintStatus.COMPLETED);
        sprintRepository.save(sprint);
        auditLogService.record(AuditEventType.SPRINT_COMPLETED, actor, sprint.getProject(), null,
            "Sprint completed: " + sprint.getName());
        return SprintResponse.from(sprint);
    }

    @Transactional(readOnly = true)
    public List<SprintResponse> listByProject(Long projectId) {
        return sprintRepository.findAllByProjectId(projectId).stream().map(SprintResponse::from).toList();
    }

    public Sprint loadById(Long id) {
        return sprintRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sprint not found: " + id));
    }
}
