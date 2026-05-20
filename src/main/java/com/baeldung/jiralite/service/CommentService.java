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
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    private final TaskRepository taskRepository;

    private final CurrentUserService currentUserService;

    private final AuditLogService auditLogService;

    public CommentService(CommentRepository commentRepository,
            TaskRepository taskRepository,
            CurrentUserService currentUserService,
            AuditLogService auditLogService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public CommentResponse addComment(Long taskId, CommentRequest request) {
        User caller = currentUserService.getCurrentUser();
        Task task = findTask(taskId);
        boolean isMember = caller.getProjects().stream()
            .anyMatch(p -> p.getId().equals(task.getProject().getId()));
        if (!isMember) {
            throw new ForbiddenException("You are not a member of this project");
        }
        Comment comment = new Comment();
        comment.setTask(task);
        comment.setAuthor(caller);
        comment.setBody(request.getBody());
        comment.setCreatedAt(Instant.now());
        commentRepository.save(comment);
        auditLogService.record(AuditEventType.COMMENT_ADDED, caller,
            task.getProject().getId(), task.getId(),
            "Comment added by " + caller.getUsername());
        return toResponse(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private Task findTask(Long taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getTask().getId(),
            comment.getAuthor().getId(),
            comment.getBody(),
            comment.getCreatedAt()
        );
    }
}
