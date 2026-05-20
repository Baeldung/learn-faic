package com.baeldung.jiralite.task;

import com.baeldung.jiralite.web.ConflictException;

public enum TaskStatus {
    OPEN, IN_PROGRESS, IN_REVIEW, DONE, CLOSED;

    public void validateTransitionTo(TaskStatus next) {
        boolean valid = switch (this) {
            case OPEN -> next == IN_PROGRESS;
            case IN_PROGRESS -> next == IN_REVIEW;
            case IN_REVIEW -> next == DONE;
            case DONE -> next == CLOSED;
            case CLOSED -> next == OPEN;
        };
        if (!valid) {
            throw new ConflictException("Invalid transition from " + this + " to " + next);
        }
    }
}
