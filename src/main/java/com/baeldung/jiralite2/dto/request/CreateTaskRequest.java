package com.baeldung.jiralite2.dto.request;

import com.baeldung.jiralite2.domain.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateTaskRequest(
    @NotNull Long projectId,
    @NotBlank String title,
    String description,
    Priority priority,
    Long assigneeId,
    Long sprintId,
    LocalDate dueDate
) {}
