package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.TaskPriority;
import java.time.LocalDate;

public record TaskUpdateRequest(
    String title,
    String description,
    TaskPriority priority,
    Long assigneeId,
    Long sprintId,
    LocalDate dueDate
) {}
