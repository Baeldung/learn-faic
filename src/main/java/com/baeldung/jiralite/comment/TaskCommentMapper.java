package com.baeldung.jiralite.comment;

import org.springframework.stereotype.Component;

@Component
public class TaskCommentMapper {

    public CommentResponse toResponse(TaskComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTask().getId(),
                comment.getAuthor().getId(),
                comment.getBody(),
                comment.getCreatedAt());
    }
}
