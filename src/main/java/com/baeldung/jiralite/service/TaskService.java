package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.AuditEventType;
import com.baeldung.jiralite.domain.Project;
import com.baeldung.jiralite.domain.Task;
import com.baeldung.jiralite.domain.TaskPriority;
import com.baeldung.jiralite.domain.TaskStatus;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.dto.TaskRequest;
import com.baeldung.jiralite.dto.TaskResponse;
import com.baeldung.jiralite.dto.TaskUpdateRequest;
import com.baeldung.jiralite.dto.TransitionRequest;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import com.baeldung.jiralite.repository.ProjectRepository;
import com.baeldung.jiralite.repository.SprintRepository;
import com.baeldung.jiralite.repository.TaskRepository;
import com.baeldung.jiralite.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private static final String ENTITY_TYPE = "TASK";
    private static final String TASK_NOT_FOUND = "Task not found";
    private static final String ASSIGNEE_NOT_FOUND = "Assignee not found";
    private static final String SPRINT_NOT_FOUND = "Sprint not found";
    private static final String FIELD_PROJECT = "project";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_ASSIGNEE = "assignee";
    private static final String FIELD_PRIORITY = "priority";
    private static final String FIELD_SPRINT = "sprint";
    private static final String FIELD_ID = "id";

    @Autowired
    private TaskRepository taskRepo;

    @Autowired
    private ProjectRepository projectRepo;

    @Autowired
    private SprintRepository sprintRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private TaskTransitionValidator transitionValidator;

    @Transactional
    public TaskResponse createTask(TaskRequest request, User actor) {
        Project project = projectRepo.findById(request.getProjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        Task task = new Task();
        task.setProject(project);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setReporter(actor);
        if (request.getAssigneeId() != null) {
            task.setAssignee(userRepo.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException(ASSIGNEE_NOT_FOUND)));
        }
        if (request.getSprintId() != null) {
            task.setSprint(sprintRepo.findById(request.getSprintId())
                .orElseThrow(() -> new ResourceNotFoundException(SPRINT_NOT_FOUND)));
        }
        taskRepo.save(task);
        auditLogService.log(AuditEventType.TASK_CREATED, actor, ENTITY_TYPE, task.getId(), project);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, TaskUpdateRequest request, User actor) {
        Task task = findTask(taskId);
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
            task.setAssignee(userRepo.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException(ASSIGNEE_NOT_FOUND)));
        }
        if (request.getSprintId() != null) {
            task.setSprint(sprintRepo.findById(request.getSprintId())
                .orElseThrow(() -> new ResourceNotFoundException(SPRINT_NOT_FOUND)));
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        return toResponse(taskRepo.save(task));
    }

    @Transactional
    public TaskResponse transitionTask(Long taskId, TransitionRequest request, User actor) {
        Task task = findTask(taskId);
        TaskStatus to = request.status();
        transitionValidator.validate(task, to, actor);
        task.setStatus(to);
        taskRepo.save(task);
        auditLogService.log(AuditEventType.TASK_STATUS_CHANGED, actor, ENTITY_TYPE, taskId, task.getProject());
        return toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(Long projectId, TaskStatus status, Long assigneeId,
        TaskPriority priority, Long sprintId) {
        Specification<Task> spec = buildSpec(projectId, status, assigneeId, priority, sprintId);
        return taskRepo.findAll(spec).stream().map(this::toResponse).toList();
    }

    private Specification<Task> buildSpec(Long projectId, TaskStatus status, Long assigneeId,
        TaskPriority priority, Long sprintId) {
        return Specification
            .where(hasProject(projectId))
            .and(status != null ? hasStatus(status) : null)
            .and(assigneeId != null ? hasAssignee(assigneeId) : null)
            .and(priority != null ? hasPriority(priority) : null)
            .and(sprintId != null ? hasSprint(sprintId) : null);
    }

    private Specification<Task> hasProject(Long projectId) {
        return (root, query, cb) -> cb.equal(root.get(FIELD_PROJECT).get(FIELD_ID), projectId);
    }

    private Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) -> cb.equal(root.get(FIELD_STATUS), status);
    }

    private Specification<Task> hasAssignee(Long assigneeId) {
        return (root, query, cb) -> cb.equal(root.get(FIELD_ASSIGNEE).get(FIELD_ID), assigneeId);
    }

    private Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, cb) -> cb.equal(root.get(FIELD_PRIORITY), priority);
    }

    private Specification<Task> hasSprint(Long sprintId) {
        return (root, query, cb) -> cb.equal(root.get(FIELD_SPRINT).get(FIELD_ID), sprintId);
    }

    private Task findTask(Long taskId) {
        return taskRepo.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException(TASK_NOT_FOUND));
    }

    private TaskResponse toResponse(Task task) {
        TaskResponse r = new TaskResponse();
        r.setId(task.getId());
        r.setProjectId(task.getProject().getId());
        r.setTitle(task.getTitle());
        r.setDescription(task.getDescription());
        r.setStatus(task.getStatus());
        r.setPriority(task.getPriority());
        r.setAssigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null);
        r.setReporterId(task.getReporter().getId());
        r.setSprintId(task.getSprint() != null ? task.getSprint().getId() : null);
        r.setDueDate(task.getDueDate());
        return r;
    }
}
