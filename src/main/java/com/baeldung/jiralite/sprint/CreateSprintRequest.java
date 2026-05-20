package com.baeldung.jiralite.sprint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateSprintRequest(
        @NotNull Long projectId,
        @NotBlank String name,
        LocalDate startDate,
        LocalDate endDate) { }
