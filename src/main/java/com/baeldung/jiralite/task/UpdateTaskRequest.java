package com.baeldung.jiralite.task;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @Size(max = 255) String title,
        @Size(max = 2000) String description,
        Priority priority,
        Long assigneeId,
        Long sprintId,
        LocalDate dueDate) {
}
