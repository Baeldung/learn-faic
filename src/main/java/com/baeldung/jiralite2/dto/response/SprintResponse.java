package com.baeldung.jiralite2.dto.response;

import com.baeldung.jiralite2.domain.Sprint;
import java.time.LocalDate;

public record SprintResponse(Long id, Long projectId, String name, LocalDate startDate, LocalDate endDate, String status) {
    public static SprintResponse from(Sprint s) {
        return new SprintResponse(s.getId(), s.getProject().getId(), s.getName(),
            s.getStartDate(), s.getEndDate(), s.getStatus().name());
    }
}
