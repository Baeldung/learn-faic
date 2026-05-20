package com.baeldung.jiralite.sprint;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSprintRequest(
        @NotNull Long projectId,
        @NotBlank @Size(max = 255) String name,
        LocalDate startDate,
        LocalDate endDate) {
}
