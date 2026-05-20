package com.baeldung.jiralite.comment;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baeldung.jiralite.audit.AuditEventType;
import com.baeldung.jiralite.audit.AuditService;
import com.baeldung.jiralite.security.CurrentUser;
import com.baeldung.jiralite.task.Task;
import com.baeldung.jiralite.task.TaskService;
import com.baeldung.jiralite.user.User;

@Service
@Transactional
public class CommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskService taskService;
    private final TaskCommentMapper taskCommentMapper;
    private final AuditService auditService;
    private final CurrentUser currentUser;

    public CommentService(TaskCommentRepository taskCommentRepository, TaskService taskService,
            TaskCommentMapper taskCommentMapper, AuditService auditService, CurrentUser currentUser) {
        this.taskCommentRepository = taskCommentRepository;
        this.taskService = taskService;
        this.taskCommentMapper = taskCommentMapper;
        this.auditService = auditService;
        this.currentUser = currentUser;
    }

    public CommentResponse addComment(Long taskId, CreateCommentRequest request) {
        User actor = currentUser.get();
        Task task = taskService.requireAccessibleTask(taskId);
        TaskComment comment = new TaskComment(task, actor, request.body(), Instant.now());
        TaskComment saved = taskCommentRepository.save(comment);
        auditService.record(AuditEventType.COMMENT_ADDED, actor, task.getProject().getId(), taskId);
        return taskCommentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(Long taskId) {
        taskService.requireAccessibleTask(taskId);
        return taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(taskCommentMapper::toResponse)
                .toList();
    }
}
