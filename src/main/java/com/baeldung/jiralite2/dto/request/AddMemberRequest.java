package com.baeldung.jiralite2.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(@NotNull Long userId) {}
