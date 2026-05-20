package com.baeldung.jiralite.exception;

import com.baeldung.jiralite.domain.TaskStatus;

public class InvalidTransitionException extends RuntimeException {

    public InvalidTransitionException(TaskStatus from, TaskStatus to) {
        super("Invalid transition: " + from + " -> " + to);
    }
}
