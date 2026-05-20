package com.baeldung.jiralite2.service;

import com.baeldung.jiralite2.domain.Comment;
import com.baeldung.jiralite2.domain.Task;
import com.baeldung.jiralite2.domain.User;
import com.baeldung.jiralite2.domain.enums.AuditEventType;
import com.baeldung.jiralite2.dto.request.AddCommentRequest;
import com.baeldung.jiralite2.dto.response.CommentResponse;
import com.baeldung.jiralite2.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskService taskService;
    private final UserService userService;
    private final AuditLogService auditLogService;

    public CommentService(CommentRepository commentRepository, TaskService taskService,
                          UserService userService, AuditLogService auditLogService) {
        this.commentRepository = commentRepository;
        this.taskService = taskService;
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public CommentResponse add(Long taskId, AddCommentRequest req, String actorUsername) {
        User actor = userService.loadByUsername(actorUsername);
        Task task = taskService.loadById(taskId);
        Comment comment = new Comment(task, actor, req.body());
        commentRepository.save(comment);
        auditLogService.record(AuditEventType.COMMENT_ADDED, actor, task.getProject(), task,
            "Comment added by " + actor.getUsername());
        return CommentResponse.from(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listByTask(Long taskId) {
        return commentRepository.findAllByTaskIdOrderByCreatedAtAsc(taskId)
            .stream().map(CommentResponse::from).toList();
    }
}
