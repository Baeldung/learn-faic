package com.baeldung.jiralite.comment;

import java.time.Instant;

public record CommentResponse(Long id, Long taskId, Long authorId, String body, Instant createdAt) {
}
