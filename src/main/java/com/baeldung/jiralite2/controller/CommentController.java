package com.baeldung.jiralite2.controller;

import com.baeldung.jiralite2.dto.request.AddCommentRequest;
import com.baeldung.jiralite2.dto.response.CommentResponse;
import com.baeldung.jiralite2.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse add(@PathVariable Long taskId,
                                @Valid @RequestBody AddCommentRequest req,
                                @AuthenticationPrincipal UserDetails principal) {
        return commentService.add(taskId, req, principal.getUsername());
    }

    @GetMapping
    public List<CommentResponse> list(@PathVariable Long taskId) {
        return commentService.listByTask(taskId);
    }
}
