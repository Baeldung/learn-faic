package com.baeldung.jiralite2.service;

import com.baeldung.jiralite2.domain.*;
import com.baeldung.jiralite2.domain.enums.*;
import com.baeldung.jiralite2.dto.request.CreateTaskRequest;
import com.baeldung.jiralite2.dto.request.TransitionRequest;
import com.baeldung.jiralite2.dto.request.UpdateTaskRequest;
import com.baeldung.jiralite2.dto.response.TaskResponse;
import com.baeldung.jiralite2.exception.AccessDeniedException;
import com.baeldung.jiralite2.exception.InvalidTransitionException;
import com.baeldung.jiralite2.exception.ResourceNotFoundException;
import com.baeldung.jiralite2.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

@Service
public class TaskService {

    private static final Set<TaskStatus> CLOSE_ONLY_ROLES_REQUIRED = Set.of(TaskStatus.CLOSED);

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final SprintService sprintService;
    private final AuditLogService auditLogService;

    public TaskService(TaskRepository taskRepository, ProjectService projectService,
                       UserService userService, SprintService sprintService,
                       AuditLogService auditLogService) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.sprintService = sprintService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public TaskResponse create(CreateTaskRequest req, String actorUsername) {
        User actor = userService.loadByUsername(actorUsername);
        Project project = projectService.loadById(req.projectId());

        Task task = new Task();
        task.setProject(project);
        task.setTitle(req.title());
        task.setDescription(req.description());
        task.setPriority(req.priority() != null ? req.priority() : Priority.MEDIUM);
        task.setReporter(actor);
        task.setDueDate(req.dueDate());

        if (req.assigneeId() != null) {
            task.setAssignee(userService.loadById(req.assigneeId()));
        }
        if (req.sprintId() != null) {
            task.setSprint(sprintService.loadById(req.sprintId()));
        }

        taskRepository.save(task);
        auditLogService.record(AuditEventType.TASK_CREATED, actor, project, task,
            "Task created: " + task.getTitle());
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse update(Long taskId, UpdateTaskRequest req, String actorUsername) {
        User actor = userService.loadByUsername(actorUsername);
        Task task = loadById(taskId);

        if (req.title() != null) task.setTitle(req.title());
        if (req.description() != null) task.setDescription(req.description());
        if (req.priority() != null) task.setPriority(req.priority());
        if (req.dueDate() != null) task.setDueDate(req.dueDate());

        if (req.assigneeId() != null) {
            task.setAssignee(userService.loadById(req.assigneeId()));
            auditLogService.record(AuditEventType.TASK_ASSIGNED, actor, task.getProject(), task,
                "Assigned to " + task.getAssignee().getUsername());
        }
        if (req.sprintId() != null) {
            task.setSprint(sprintService.loadById(req.sprintId()));
        }

        taskRepository.save(task);
        auditLogService.record(AuditEventType.TASK_UPDATED, actor, task.getProject(), task, "Task updated");
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse transition(Long taskId, TransitionRequest req, String actorUsername) {
        User actor = userService.loadByUsername(actorUsername);
        Task task = loadById(taskId);
        TaskStatus from = task.getStatus();
        TaskStatus to = req.status();

        validateTransition(from, to, actor);

        task.setStatus(to);
        taskRepository.save(task);
        auditLogService.record(AuditEventType.TASK_STATUS_CHANGED, actor, task.getProject(), task,
            from + " -> " + to);
        return TaskResponse.from(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> list(Long projectId, TaskStatus status, Long assigneeId,
                                    Priority priority, Long sprintId) {
        return taskRepository.findWithFilters(projectId, status, assigneeId, priority, sprintId)
            .stream().map(TaskResponse::from).toList();
    }

    public Task loadById(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }

    private void validateTransition(TaskStatus from, TaskStatus to, User actor) {
        boolean valid = switch (from) {
            case OPEN -> to == TaskStatus.IN_PROGRESS;
            case IN_PROGRESS -> to == TaskStatus.IN_REVIEW;
            case IN_REVIEW -> to == TaskStatus.DONE;
            case DONE -> to == TaskStatus.CLOSED;
            case CLOSED -> to == TaskStatus.OPEN;
        };
        if (!valid) {
            throw new InvalidTransitionException("Invalid transition: " + from + " -> " + to);
        }
        if (to == TaskStatus.CLOSED || (from == TaskStatus.CLOSED && to == TaskStatus.OPEN)) {
            Role role = actor.getRole();
            if (role != Role.ADMIN && role != Role.MANAGER) {
                throw new AccessDeniedException("Only ADMIN or MANAGER can close/reopen tasks");
            }
        }
    }
}
