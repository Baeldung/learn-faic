package com.baeldung.jiralite.controller;

import com.baeldung.jiralite.dto.CommentRequest;
import com.baeldung.jiralite.dto.CommentResponse;
import com.baeldung.jiralite.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks/{taskId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long projectId,
                                                       @PathVariable Long taskId,
                                                       @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.addComment(projectId, taskId, request));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> listComments(@PathVariable Long projectId,
                                                               @PathVariable Long taskId) {
        return ResponseEntity.ok(commentService.listComments(projectId, taskId));
    }
}
