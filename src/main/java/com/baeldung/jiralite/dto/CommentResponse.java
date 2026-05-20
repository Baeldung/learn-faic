package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.Comment;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long id;
    private Long taskId;
    private UserResponse author;
    private String content;
    private LocalDateTime createdAt;

    public static CommentResponse from(Comment comment) {
        CommentResponse r = new CommentResponse();
        r.setId(comment.getId());
        r.setTaskId(comment.getTask().getId());
        r.setAuthor(UserResponse.from(comment.getAuthor()));
        r.setContent(comment.getContent());
        r.setCreatedAt(comment.getCreatedAt());
        return r;
    }
}
