package com.baeldung.jiralite.task;

import jakarta.validation.constraints.NotNull;

public record TransitionRequest(@NotNull TaskStatus status) { }
