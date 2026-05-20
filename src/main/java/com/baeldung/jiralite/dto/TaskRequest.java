package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TaskRequest(
    @NotNull Long projectId,
    @NotBlank String title,
    String description,
    @NotNull TaskPriority priority,
    Long assigneeId,
    Long sprintId,
    LocalDate dueDate
) {}
