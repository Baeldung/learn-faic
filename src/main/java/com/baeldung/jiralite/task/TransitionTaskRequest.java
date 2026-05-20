package com.baeldung.jiralite.task;

import jakarta.validation.constraints.NotNull;

public record TransitionTaskRequest(@NotNull TaskStatus status) {
}
