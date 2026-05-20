package com.baeldung.jiralite2.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateSprintRequest(
    @NotBlank String name,
    LocalDate startDate,
    LocalDate endDate
) {}
