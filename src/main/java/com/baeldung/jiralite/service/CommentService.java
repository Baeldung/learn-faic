package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.Comment;
import com.baeldung.jiralite.domain.Task;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.domain.enums.Role;
import com.baeldung.jiralite.dto.CommentRequest;
import com.baeldung.jiralite.dto.CommentResponse;
import com.baeldung.jiralite.exception.ForbiddenException;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import com.baeldung.jiralite.repository.CommentRepository;
import com.baeldung.jiralite.repository.TaskRepository;
import com.baeldung.jiralite.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final SecurityUtils securityUtils;
    private final AuditLogService auditLogService;

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository,
                          ProjectService projectService, SecurityUtils securityUtils,
                          AuditLogService auditLogService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.securityUtils = securityUtils;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public CommentResponse addComment(Long projectId, Long taskId, CommentRequest request) {
        projectService.findProjectWithAccess(projectId);
        User actor = securityUtils.getCurrentUser();
        if (actor.getRole() == Role.VIEWER) {
            throw new ForbiddenException("VIEWERs cannot add comments");
        }
        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        Comment comment = Comment.builder()
                .task(task)
                .author(actor)
                .content(request.getContent())
                .build();
        comment = commentRepository.save(comment);
        auditLogService.log("COMMENT_ADDED", actor.getId(), projectId, taskId,
                "Comment added by " + actor.getUsername());
        return CommentResponse.from(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(Long projectId, Long taskId) {
        projectService.findProjectWithAccess(projectId);
        taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(CommentResponse::from).toList();
    }
}
