package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.Sprint;
import com.baeldung.jiralite.domain.SprintStatus;

import java.time.LocalDate;

public record SprintResponse(
    Long id,
    Long projectId,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    SprintStatus status
) {
    public static SprintResponse from(Sprint sprint) {
        return new SprintResponse(
            sprint.getId(),
            sprint.getProject().getId(),
            sprint.getName(),
            sprint.getStartDate(),
            sprint.getEndDate(),
            sprint.getStatus()
        );
    }
}
