package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.Comment;

import java.time.Instant;

public record CommentResponse(
    Long id,
    Long taskId,
    UserResponse author,
    String body,
    Instant createdAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getTask().getId(),
            UserResponse.from(comment.getAuthor()),
            comment.getBody(),
            comment.getCreatedAt()
        );
    }
}
