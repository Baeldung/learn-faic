package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.AuditEventType;
import com.baeldung.jiralite.domain.Project;
import com.baeldung.jiralite.domain.Sprint;
import com.baeldung.jiralite.domain.Task;
import com.baeldung.jiralite.domain.TaskPriority;
import com.baeldung.jiralite.domain.TaskStatus;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.dto.TaskRequest;
import com.baeldung.jiralite.dto.TaskResponse;
import com.baeldung.jiralite.dto.TaskUpdateRequest;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskServiceDeps deps;

    private final CurrentUserService currentUserService;

    private final AuditLogService auditLogService;

    public TaskService(TaskServiceDeps deps,
            CurrentUserService currentUserService,
            AuditLogService auditLogService) {
        this.deps = deps;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        User caller = currentUserService.getCurrentUser();
        Project project = findProject(request.getProjectId());
        Task task = new Task();
        task.setProject(project);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.OPEN);
        task.setPriority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM);
        task.setReporter(caller);
        task.setAssignee(resolveUser(request.getAssigneeId()));
        task.setSprint(resolveSprint(request.getSprintId()));
        task.setDueDate(request.getDueDate());
        deps.getTaskRepository().save(task);
        auditLogService.record(AuditEventType.TASK_CREATED, caller, project.getId(), task.getId(),
            "Task created: " + task.getTitle());
        return TaskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, TaskUpdateRequest request) {
        Task task = findTask(taskId);
        applyUpdates(task, request);
        deps.getTaskRepository().save(task);
        return TaskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse transition(Long taskId, TaskStatus newStatus) {
        User caller = currentUserService.getCurrentUser();
        Task task = findTask(taskId);
        TaskTransitionValidator.validate(task.getStatus(), newStatus, caller);
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);
        deps.getTaskRepository().save(task);
        auditLogService.record(AuditEventType.TASK_STATUS_CHANGED, caller,
            task.getProject().getId(), task.getId(),
            "Status changed: " + oldStatus + " -> " + newStatus);
        return TaskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(Long projectId, TaskStatus status,
            Long assigneeId, TaskPriority priority, Long sprintId) {
        return deps.getTaskRepository()
            .findAllByFilters(projectId, status, assigneeId, priority, sprintId)
            .stream()
            .map(TaskMapper::toResponse)
            .collect(Collectors.toList());
    }

    private void applyUpdates(Task task, TaskUpdateRequest request) {
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getAssigneeId() != null) {
            task.setAssignee(resolveUser(request.getAssigneeId()));
        }
        if (request.getSprintId() != null) {
            task.setSprint(resolveSprint(request.getSprintId()));
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
    }

    private Task findTask(Long taskId) {
        return deps.getTaskRepository().findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
    }

    private Project findProject(Long projectId) {
        return deps.getProjectRepository().findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }

    private User resolveUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return deps.getUserRepository().findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private Sprint resolveSprint(Long sprintId) {
        if (sprintId == null) {
            return null;
        }
        return deps.getSprintRepository().findById(sprintId)
            .orElseThrow(() -> new ResourceNotFoundException("Sprint not found: " + sprintId));
    }
}
