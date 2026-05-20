package com.baeldung.jiralite.task;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baeldung.jiralite.audit.AuditEventType;
import com.baeldung.jiralite.audit.AuditService;
import com.baeldung.jiralite.project.Project;
import com.baeldung.jiralite.security.CurrentUser;
import com.baeldung.jiralite.user.Role;
import com.baeldung.jiralite.user.User;
import com.baeldung.jiralite.web.ForbiddenException;
import com.baeldung.jiralite.web.NotFoundException;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskRefResolver refs;
    private final TaskMapper taskMapper;
    private final AuditService auditService;
    private final CurrentUser currentUser;

    public TaskService(TaskRepository taskRepository, TaskRefResolver refs,
            TaskMapper taskMapper, AuditService auditService, CurrentUser currentUser) {
        this.taskRepository = taskRepository;
        this.refs = refs;
        this.taskMapper = taskMapper;
        this.auditService = auditService;
        this.currentUser = currentUser;
    }

    public TaskResponse createTask(CreateTaskRequest request) {
        User actor = currentUser.get();
        Project project = refs.requireProjectAccess(request.projectId(), actor.getId());

        Task task = new Task();
        task.setProject(project);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setReporter(actor);
        task.setDueDate(request.dueDate());

        if (request.assigneeId() != null) {
            task.setAssignee(refs.resolveUser(request.assigneeId()));
        }
        if (request.sprintId() != null) {
            task.setSprint(refs.resolveSprint(request.sprintId()));
        }

        Task saved = taskRepository.save(task);
        auditService.record(AuditEventType.TASK_CREATED, actor, project.getId(), saved.getId());
        return taskMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(Long projectId, TaskStatus status, Long assigneeId,
            Priority priority, Long sprintId) {
        User actor = currentUser.get();
        refs.requireProjectAccess(projectId, actor.getId());
        return taskRepository.findFiltered(projectId, status, assigneeId, priority, sprintId).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId) {
        Task task = requireAccessibleTask(taskId);
        return taskMapper.toResponse(task);
    }

    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        User actor = currentUser.get();
        Task task = requireAccessibleTask(taskId);

        if (request.title() != null) {
            task.setTitle(request.title());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.assigneeId() != null) {
            task.setAssignee(refs.resolveUser(request.assigneeId()));
        }
        if (request.sprintId() != null) {
            task.setSprint(refs.resolveSprint(request.sprintId()));
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }

        auditService.record(AuditEventType.TASK_UPDATED, actor, task.getProject().getId(), taskId);
        return taskMapper.toResponse(task);
    }

    public TaskResponse transition(Long taskId, TransitionTaskRequest request) {
        User actor = currentUser.get();
        Task task = requireAccessibleTask(taskId);

        TaskStatus current = task.getStatus();
        TaskStatus next = request.status();
        current.validateTransitionTo(next);
        assertPrivilegedTransition(actor, current, next);

        task.setStatus(next);
        auditService.record(AuditEventType.TASK_STATUS_CHANGED, actor, task.getProject().getId(), taskId);
        if (current == TaskStatus.CLOSED) {
            auditService.record(AuditEventType.TASK_REOPENED, actor, task.getProject().getId(), taskId);
        }
        return taskMapper.toResponse(task);
    }

    public Task requireAccessibleTask(Long taskId) {
        Task task = findEntity(taskId);
        refs.requireProjectAccess(task.getProject(), currentUser.getId());
        return task;
    }

    public Task findEntity(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task " + taskId + " not found"));
    }

    private void assertPrivilegedTransition(User actor, TaskStatus current, TaskStatus next) {
        Role role = actor.getRole();
        boolean privileged = role == Role.ADMIN || role == Role.MANAGER;
        if (privileged) {
            return;
        }
        if (next == TaskStatus.CLOSED) {
            throw new ForbiddenException("Only MANAGER or ADMIN can close a task");
        }
        if (current == TaskStatus.CLOSED && next == TaskStatus.OPEN) {
            throw new ForbiddenException("Only MANAGER or ADMIN can reopen a task");
        }
    }
}
