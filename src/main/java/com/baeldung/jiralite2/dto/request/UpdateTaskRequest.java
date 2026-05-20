package com.baeldung.jiralite2.dto.request;

import com.baeldung.jiralite2.domain.enums.Priority;
import java.time.LocalDate;

public record UpdateTaskRequest(
    String title,
    String description,
    Priority priority,
    Long assigneeId,
    Long sprintId,
    LocalDate dueDate
) {}
