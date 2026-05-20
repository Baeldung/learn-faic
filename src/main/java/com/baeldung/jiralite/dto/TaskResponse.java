package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.Task;
import com.baeldung.jiralite.domain.TaskPriority;
import com.baeldung.jiralite.domain.TaskStatus;

import java.time.LocalDate;

public record TaskResponse(
    Long id,
    Long projectId,
    String title,
    String description,
    TaskStatus status,
    TaskPriority priority,
    UserResponse assignee,
    UserResponse reporter,
    Long sprintId,
    LocalDate dueDate
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getProject().getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getPriority(),
            task.getAssignee() != null ? UserResponse.from(task.getAssignee()) : null,
            UserResponse.from(task.getReporter()),
            task.getSprint() != null ? task.getSprint().getId() : null,
            task.getDueDate()
        );
    }
}
