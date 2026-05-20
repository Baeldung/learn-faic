package com.baeldung.jiralite.sprint;

import java.time.LocalDate;

public record SprintResponse(
        Long id,
        Long projectId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        SprintStatus status) {
}
