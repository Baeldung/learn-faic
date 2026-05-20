package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.AuditEventType;
import com.baeldung.jiralite.domain.Comment;
import com.baeldung.jiralite.domain.Task;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.dto.CommentRequest;
import com.baeldung.jiralite.dto.CommentResponse;
import com.baeldung.jiralite.exception.ForbiddenException;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import com.baeldung.jiralite.repository.CommentRepository;
import com.baeldung.jiralite.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final AuditLogService auditLogService;

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository,
                          AuditLogService auditLogService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public CommentResponse addComment(Long taskId, CommentRequest req, User actor) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        boolean isMember = task.getProject().getMembers().stream()
            .anyMatch(m -> m.getId().equals(actor.getId()));
        if (!isMember) {
            throw new ForbiddenException("Only project members can add comments");
        }

        Comment comment = new Comment(task, actor, req.body());
        commentRepository.save(comment);
        auditLogService.log(AuditEventType.COMMENT_ADDED, actor, "TASK", taskId, task.getProject());
        return CommentResponse.from(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
            .map(CommentResponse::from)
            .toList();
    }
}
