package com.baeldung.jiralite.domain;

import java.util.Map;
import java.util.Set;

public enum TaskStatus {
    OPEN, IN_PROGRESS, IN_REVIEW, DONE, CLOSED;

    private static final Map<TaskStatus, Set<TaskStatus>> VALID_TRANSITIONS = Map.of(
        OPEN,        Set.of(IN_PROGRESS),
        IN_PROGRESS, Set.of(IN_REVIEW),
        IN_REVIEW,   Set.of(DONE),
        DONE,        Set.of(CLOSED),
        CLOSED,      Set.of(OPEN)
    );

    public boolean canTransitionTo(TaskStatus next) {
        return VALID_TRANSITIONS.getOrDefault(this, Set.of()).contains(next);
    }
}
