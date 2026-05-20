package com.baeldung.jiralite.domain;

public enum TaskStatus {
    OPEN, IN_PROGRESS, IN_REVIEW, DONE, CLOSED;

    public boolean canTransitionTo(TaskStatus target) {
        return switch (this) {
            case OPEN -> target == IN_PROGRESS;
            case IN_PROGRESS -> target == IN_REVIEW;
            case IN_REVIEW -> target == DONE;
            case DONE -> target == CLOSED;
            case CLOSED -> target == OPEN;
        };
    }
}
