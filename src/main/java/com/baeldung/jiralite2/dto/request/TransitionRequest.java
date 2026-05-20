package com.baeldung.jiralite2.dto.request;

import com.baeldung.jiralite2.domain.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TransitionRequest(@NotNull TaskStatus status) {}
