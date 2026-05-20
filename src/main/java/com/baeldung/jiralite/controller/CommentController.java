package com.baeldung.jiralite.controller;

import com.baeldung.jiralite.dto.CommentRequest;
import com.baeldung.jiralite.dto.CommentResponse;
import com.baeldung.jiralite.security.UserPrincipal;
import com.baeldung.jiralite.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks/{taskId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long taskId,
                                                      @Valid @RequestBody CommentRequest req,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(commentService.addComment(taskId, req, principal.getUser()));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> listComments(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentService.listComments(taskId));
    }
}
