package com.baeldung.jiralite2.dto.response;

import com.baeldung.jiralite2.domain.Comment;
import java.time.Instant;

public record CommentResponse(Long id, Long taskId, UserResponse author, String body, Instant createdAt) {
    public static CommentResponse from(Comment c) {
        return new CommentResponse(c.getId(), c.getTask().getId(), UserResponse.from(c.getAuthor()), c.getBody(), c.getCreatedAt());
    }
}
