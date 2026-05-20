package com.baeldung.jiralite.task;

import java.time.LocalDate;

public record TaskResponse(
        Long id,
        Long projectId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Long assigneeId,
        Long reporterId,
        Long sprintId,
        LocalDate dueDate) { }
