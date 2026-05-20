package com.baeldung.jiralite.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateTaskRequest(
        @NotBlank String title,
        String description,
        @NotNull TaskPriority priority,
        Long assigneeId,
        Long sprintId,
        LocalDate dueDate) { }
