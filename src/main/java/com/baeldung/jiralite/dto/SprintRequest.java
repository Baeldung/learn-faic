package com.baeldung.jiralite.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class SprintRequest {

    @NotBlank
    private String name;

    private LocalDate startDate;

    private LocalDate endDate;

    public SprintRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
