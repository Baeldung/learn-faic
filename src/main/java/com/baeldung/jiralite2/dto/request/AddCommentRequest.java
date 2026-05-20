package com.baeldung.jiralite2.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddCommentRequest(@NotBlank String body) {}
