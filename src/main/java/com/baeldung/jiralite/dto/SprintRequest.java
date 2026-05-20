package com.baeldung.jiralite.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record SprintRequest(@NotBlank String name, LocalDate startDate, LocalDate endDate) {
}
