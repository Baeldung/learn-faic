package com.baeldung.jiralite.comment;

import com.baeldung.jiralite.audit.AuditEntityType;
import com.baeldung.jiralite.audit.AuditEventType;
import com.baeldung.jiralite.audit.AuditLogger;
import com.baeldung.jiralite.audit.AuditWrite;
import com.baeldung.jiralite.task.Task;
import com.baeldung.jiralite.task.TaskService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskService taskService;
    private final CommentMapper commentMapper;
    private final AuditLogger audit;

    public CommentService(CommentRepository commentRepository, TaskService taskService, CommentMapper commentMapper,
                          AuditLogger audit) {
        this.commentRepository = commentRepository;
        this.taskService = taskService;
        this.commentMapper = commentMapper;
        this.audit = audit;
    }

    @Transactional
    public CommentResponse addComment(Long taskId, String body) {
        Task task = taskService.loadVisibleTask(taskId);
        Comment saved = commentRepository.save(new Comment(task, audit.currentEntity(), body));
        audit.log(new AuditWrite(AuditEventType.COMMENT_ADDED, AuditEntityType.COMMENT, saved.getId(),
                task.getProject().getId(), "taskId=" + taskId));
        return commentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listForTask(Long taskId) {
        taskService.loadVisibleTask(taskId);
        return commentRepository.findByTaskId(taskId).stream().map(commentMapper::toResponse).toList();
    }
}
