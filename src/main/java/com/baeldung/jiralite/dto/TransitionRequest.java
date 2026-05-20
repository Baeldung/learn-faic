package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TransitionRequest(@NotNull TaskStatus status) {}
