package com.baeldung.jiralite.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(@NotBlank String body) {
}
