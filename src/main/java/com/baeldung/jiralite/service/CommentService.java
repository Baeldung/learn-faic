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
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private static final String ENTITY_TYPE = "TASK";

    @Autowired
    private CommentRepository commentRepo;

    @Autowired
    private TaskRepository taskRepo;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public CommentResponse addComment(Long taskId, CommentRequest request, User actor) {
        Task task = taskRepo.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        boolean isMember = task.getProject().getMembers().stream()
            .anyMatch(m -> m.getId().equals(actor.getId()));
        if (!isMember) {
            throw new ForbiddenException("Only project members can comment");
        }
        Comment comment = new Comment();
        comment.setTask(task);
        comment.setAuthor(actor);
        comment.setBody(request.body());
        commentRepo.save(comment);
        auditLogService.log(AuditEventType.COMMENT_ADDED, actor, ENTITY_TYPE, taskId, task.getProject());
        return toResponse(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(Long taskId) {
        return commentRepo.findByTaskIdOrderByCreatedAtAsc(taskId)
            .stream().map(this::toResponse).toList();
    }

    private CommentResponse toResponse(Comment comment) {
        CommentResponse r = new CommentResponse();
        r.setId(comment.getId());
        r.setTaskId(comment.getTask().getId());
        r.setAuthorUsername(comment.getAuthor().getUsername());
        r.setBody(comment.getBody());
        r.setCreatedAt(comment.getCreatedAt());
        return r;
    }
}
