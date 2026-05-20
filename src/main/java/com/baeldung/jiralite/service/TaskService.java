package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.*;
import com.baeldung.jiralite.dto.TaskRequest;
import com.baeldung.jiralite.dto.TaskResponse;
import com.baeldung.jiralite.dto.TaskUpdateRequest;
import com.baeldung.jiralite.dto.TransitionRequest;
import com.baeldung.jiralite.exception.ForbiddenException;
import com.baeldung.jiralite.exception.InvalidTransitionException;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import com.baeldung.jiralite.repository.ProjectRepository;
import com.baeldung.jiralite.repository.SprintRepository;
import com.baeldung.jiralite.repository.TaskRepository;
import com.baeldung.jiralite.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SprintRepository sprintRepository;
    private final AuditLogService auditLogService;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
                       UserRepository userRepository, SprintRepository sprintRepository,
                       AuditLogService auditLogService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.sprintRepository = sprintRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public TaskResponse createTask(TaskRequest req, User actor) {
        Project project = projectRepository.findById(req.projectId())
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + req.projectId()));

        Task task = new Task();
        task.setProject(project);
        task.setTitle(req.title());
        task.setDescription(req.description());
        task.setPriority(req.priority());
        task.setReporter(actor);
        task.setDueDate(req.dueDate());

        if (req.assigneeId() != null) {
            task.setAssignee(userRepository.findById(req.assigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found: " + req.assigneeId())));
        }
        if (req.sprintId() != null) {
            task.setSprint(sprintRepository.findById(req.sprintId())
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found: " + req.sprintId())));
        }

        taskRepository.save(task);
        auditLogService.log(AuditEventType.TASK_CREATED, actor, "TASK", task.getId(), project);
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, TaskUpdateRequest req, User actor) {
        Task task = findTask(taskId);

        if (req.title() != null) task.setTitle(req.title());
        if (req.description() != null) task.setDescription(req.description());
        if (req.priority() != null) task.setPriority(req.priority());
        if (req.dueDate() != null) task.setDueDate(req.dueDate());

        if (req.assigneeId() != null) {
            task.setAssignee(userRepository.findById(req.assigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found: " + req.assigneeId())));
        }
        if (req.sprintId() != null) {
            task.setSprint(sprintRepository.findById(req.sprintId())
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found: " + req.sprintId())));
        }

        taskRepository.save(task);
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse transitionTask(Long taskId, TransitionRequest req, User actor) {
        Task task = findTask(taskId);
        TaskStatus current = task.getStatus();
        TaskStatus next = req.status();

        if (!current.canTransitionTo(next)) {
            throw new InvalidTransitionException(
                "Cannot transition from " + current + " to " + next);
        }

        if (next == TaskStatus.CLOSED || current == TaskStatus.CLOSED) {
            requireManagerOrAdmin(actor);
        }

        task.setStatus(next);
        taskRepository.save(task);
        auditLogService.log(AuditEventType.TASK_STATUS_CHANGED, actor, "TASK", taskId, task.getProject());
        return TaskResponse.from(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(Long projectId, TaskStatus status, Long assigneeId,
                                        TaskPriority priority, Long sprintId) {
        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (projectId != null)
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            if (status != null)
                predicates.add(cb.equal(root.get("status"), status));
            if (assigneeId != null)
                predicates.add(cb.equal(root.get("assignee").get("id"), assigneeId));
            if (priority != null)
                predicates.add(cb.equal(root.get("priority"), priority));
            if (sprintId != null)
                predicates.add(cb.equal(root.get("sprint").get("id"), sprintId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return taskRepository.findAll(spec).stream()
            .map(TaskResponse::from)
            .toList();
    }

    private Task findTask(Long taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
    }

    private void requireManagerOrAdmin(User actor) {
        Role role = actor.getRole();
        if (role != Role.MANAGER && role != Role.ADMIN) {
            throw new ForbiddenException("Only managers and admins can perform this operation");
        }
    }
}
