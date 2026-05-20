package com.baeldung.jiralite.task;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotNull Long projectId,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String description,
        @NotNull Priority priority,
        Long assigneeId,
        Long sprintId,
        LocalDate dueDate) {
}
