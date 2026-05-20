package com.baeldung.jiralite.dto;

import com.baeldung.jiralite.domain.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public class TransitionRequest {
    @NotNull
    private TaskStatus status;

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
}
