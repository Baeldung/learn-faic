package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.Project;
import com.baeldung.jiralite.domain.Sprint;
import com.baeldung.jiralite.domain.Task;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.domain.enums.Role;
import com.baeldung.jiralite.domain.enums.TaskPriority;
import com.baeldung.jiralite.domain.enums.TaskStatus;
import com.baeldung.jiralite.dto.TaskRequest;
import com.baeldung.jiralite.dto.TaskResponse;
import com.baeldung.jiralite.dto.TaskUpdateRequest;
import com.baeldung.jiralite.dto.TransitionRequest;
import com.baeldung.jiralite.exception.ForbiddenException;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import com.baeldung.jiralite.repository.SprintRepository;
import com.baeldung.jiralite.repository.TaskRepository;
import com.baeldung.jiralite.repository.UserRepository;
import com.baeldung.jiralite.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SprintRepository sprintRepository;
    private final ProjectService projectService;
    private final SecurityUtils securityUtils;
    private final AuditLogService auditLogService;
    private final TaskTransitionValidator transitionValidator;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository,
                       SprintRepository sprintRepository, ProjectService projectService,
                       SecurityUtils securityUtils, AuditLogService auditLogService,
                       TaskTransitionValidator transitionValidator) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.sprintRepository = sprintRepository;
        this.projectService = projectService;
        this.securityUtils = securityUtils;
        this.auditLogService = auditLogService;
        this.transitionValidator = transitionValidator;
    }

    @Transactional
    public TaskResponse createTask(Long projectId, TaskRequest request) {
        Project project = projectService.findProjectWithAccess(projectId);
        User actor = securityUtils.getCurrentUser();
        requireDeveloperOrAbove(actor);

        Sprint sprint = resolveOptionalSprint(request.getSprintId(), projectId);
        User assignee = resolveOptionalUser(request.getAssigneeId());

        Task task = Task.builder()
                .project(project)
                .sprint(sprint)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .assignee(assignee)
                .reporter(actor)
                .dueDate(request.getDueDate())
                .build();

        task = taskRepository.save(task);
        auditLogService.log("TASK_CREATED", actor.getId(), projectId, task.getId(),
                "Task created: " + task.getTitle());
        return TaskResponse.from(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(Long projectId, TaskStatus status, Long assigneeId,
                                        TaskPriority priority, Long sprintId) {
        projectService.findProjectWithAccess(projectId);
        long sprintParam = sprintId == null ? -1L : sprintId;
        return taskRepository.findWithFilters(projectId, status, assigneeId, priority, sprintParam)
                .stream().map(TaskResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long projectId, Long taskId) {
        projectService.findProjectWithAccess(projectId);
        return TaskResponse.from(findTask(projectId, taskId));
    }

    @Transactional
    public TaskResponse updateTask(Long projectId, Long taskId, TaskUpdateRequest request) {
        projectService.findProjectWithAccess(projectId);
        User actor = securityUtils.getCurrentUser();
        requireDeveloperOrAbove(actor);

        Task task = findTask(projectId, taskId);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        if (request.isClearAssignee()) {
            task.setAssignee(null);
            auditLogService.log("TASK_ASSIGNEE_CHANGED", actor.getId(), projectId, taskId, "Assignee cleared");
        } else if (request.getAssigneeId() != null) {
            User newAssignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getAssigneeId()));
            task.setAssignee(newAssignee);
            auditLogService.log("TASK_ASSIGNEE_CHANGED", actor.getId(), projectId, taskId,
                    "Assignee set to " + newAssignee.getUsername());
        }

        if (request.isClearSprint()) {
            task.setSprint(null);
        } else if (request.getSprintId() != null) {
            Sprint sprint = sprintRepository.findByIdAndProjectId(request.getSprintId(), projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found: " + request.getSprintId()));
            task.setSprint(sprint);
        }

        task = taskRepository.save(task);
        auditLogService.log("TASK_UPDATED", actor.getId(), projectId, taskId,
                "Task updated: " + task.getTitle());
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse transitionTask(Long projectId, Long taskId, TransitionRequest request) {
        projectService.findProjectWithAccess(projectId);
        User actor = securityUtils.getCurrentUser();
        requireDeveloperOrAbove(actor);

        Task task = findTask(projectId, taskId);
        TaskStatus from = task.getStatus();
        TaskStatus to = request.getStatus();

        transitionValidator.validate(from, to, actor.getRole());

        task.setStatus(to);
        task = taskRepository.save(task);
        auditLogService.log("TASK_STATUS_CHANGED", actor.getId(), projectId, taskId,
                String.format("Status changed from %s to %s", from, to));
        return TaskResponse.from(task);
    }

    private Task findTask(Long projectId, Long taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found: " + taskId + " in project " + projectId));
    }

    private Sprint resolveOptionalSprint(Long sprintId, Long projectId) {
        if (sprintId == null) return null;
        return sprintRepository.findByIdAndProjectId(sprintId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found: " + sprintId));
    }

    private User resolveOptionalUser(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private void requireDeveloperOrAbove(User user) {
        if (user.getRole() == Role.VIEWER) {
            throw new ForbiddenException("VIEWERs cannot modify tasks");
        }
    }
}
