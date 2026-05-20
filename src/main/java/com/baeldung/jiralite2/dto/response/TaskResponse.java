package com.baeldung.jiralite2.dto.response;

import com.baeldung.jiralite2.domain.Task;
import java.time.LocalDate;

public record TaskResponse(
    Long id,
    Long projectId,
    String title,
    String description,
    String status,
    String priority,
    UserResponse assignee,
    UserResponse reporter,
    Long sprintId,
    LocalDate dueDate
) {
    public static TaskResponse from(Task t) {
        return new TaskResponse(
            t.getId(),
            t.getProject().getId(),
            t.getTitle(),
            t.getDescription(),
            t.getStatus().name(),
            t.getPriority().name(),
            t.getAssignee() != null ? UserResponse.from(t.getAssignee()) : null,
            UserResponse.from(t.getReporter()),
            t.getSprint() != null ? t.getSprint().getId() : null,
            t.getDueDate()
        );
    }
}
