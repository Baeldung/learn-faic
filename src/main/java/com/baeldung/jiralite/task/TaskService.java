package com.baeldung.jiralite.task;

import com.baeldung.jiralite.audit.AuditEntityType;
import com.baeldung.jiralite.audit.AuditEventType;
import com.baeldung.jiralite.audit.AuditLogger;
import com.baeldung.jiralite.audit.AuditWrite;
import com.baeldung.jiralite.project.Project;
import com.baeldung.jiralite.project.ProjectService;
import com.baeldung.jiralite.user.User;
import com.baeldung.jiralite.web.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final TaskMapper taskMapper;
    private final TaskAssociations associations;
    private final AuditLogger audit;

    public TaskService(TaskRepository taskRepository, ProjectService projectService, TaskMapper taskMapper,
                       TaskAssociations associations, AuditLogger audit) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.taskMapper = taskMapper;
        this.associations = associations;
        this.audit = audit;
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        Project project = projectService.loadVisibleProject(request.projectId());
        User reporter = audit.currentEntity();
        Task task = new Task(project, request.title(), request.description(), request.priority(), reporter);
        associations.apply(task, request.assigneeId(), request.sprintId());
        task.setDueDate(request.dueDate());
        Task saved = taskRepository.save(task);
        audit.log(new AuditWrite(AuditEventType.TASK_CREATED, AuditEntityType.TASK, saved.getId(), project.getId(), null));
        return taskMapper.toResponse(saved);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        Task task = loadVisibleTask(taskId);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        associations.apply(task, request.assigneeId(), request.sprintId());
        task.setDueDate(request.dueDate());
        audit.log(new AuditWrite(AuditEventType.TASK_UPDATED, AuditEntityType.TASK, task.getId(),
                task.getProject().getId(), null));
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse transition(Long taskId, TaskStatus to) {
        Task task = loadVisibleTask(taskId);
        TaskStatus from = task.getStatus();
        TaskWorkflow.validate(from, to, audit.currentRole());
        task.setStatus(to);
        audit.log(new AuditWrite(AuditEventType.TASK_STATUS_CHANGED, AuditEntityType.TASK, task.getId(),
                task.getProject().getId(), from + " -> " + to));
        return taskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId) {
        return taskMapper.toResponse(loadVisibleTask(taskId));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(TaskStatus status, TaskPriority priority, Long assigneeId, Long sprintId, Long projectId) {
        if (projectId != null) {
            projectService.requireVisible(projectId);
        }
        List<Long> filterIds = resolveProjectIdFilter(projectId);
        if (filterIds != null && filterIds.isEmpty()) {
            return List.of();
        }
        return taskRepository.findTasks(filterIds, status, priority, assigneeId, sprintId).stream()
                .map(taskMapper::toResponse).toList();
    }

    private List<Long> resolveProjectIdFilter(Long projectId) {
        if (audit.isAdmin()) {
            return projectId == null ? null : List.of(projectId);
        }
        List<Long> visible = projectService.visibleProjectIds();
        if (projectId == null) {
            return visible;
        }
        return visible.contains(projectId) ? List.of(projectId) : List.of();
    }

    public Task loadVisibleTask(Long taskId) {
        Task task = taskRepository.findByIdLoaded(taskId)
                .orElseThrow(() -> taskNotFound(taskId));
        try {
            projectService.requireVisible(task.getProject().getId());
        } catch (NotFoundException e) {
            throw taskNotFound(taskId);
        }
        return task;
    }

    private static NotFoundException taskNotFound(Long taskId) {
        return new NotFoundException("Task " + taskId + " not found");
    }
}
