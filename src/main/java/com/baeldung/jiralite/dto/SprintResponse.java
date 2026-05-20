package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.Sprint;
import com.baeldung.jiralite.domain.enums.SprintStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SprintResponse {
    private Long id;
    private Long projectId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private SprintStatus status;
    private LocalDateTime createdAt;

    public static SprintResponse from(Sprint sprint) {
        SprintResponse r = new SprintResponse();
        r.setId(sprint.getId());
        r.setProjectId(sprint.getProject().getId());
        r.setName(sprint.getName());
        r.setStartDate(sprint.getStartDate());
        r.setEndDate(sprint.getEndDate());
        r.setStatus(sprint.getStatus());
        r.setCreatedAt(sprint.getCreatedAt());
        return r;
    }
}
