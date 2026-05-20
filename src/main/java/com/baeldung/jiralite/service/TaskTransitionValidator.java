package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.TaskStatus;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.exception.InvalidTransitionException;

final class TaskTransitionValidator {

    private TaskTransitionValidator() {
    }

    static void validate(TaskStatus from, TaskStatus to, User caller) {
        if (from == to) {
            return;
        }
        if (to == TaskStatus.OPEN && from == TaskStatus.CLOSED) {
            RoleChecker.requireAdminOrManager(caller);
            return;
        }
        requireForwardTransition(from, to);
        if (to == TaskStatus.CLOSED) {
            RoleChecker.requireAdminOrManager(caller);
        }
    }

    private static void requireForwardTransition(TaskStatus from, TaskStatus to) {
        boolean valid = isForwardTransition(from, to);
        if (!valid) {
            throw new InvalidTransitionException("Invalid transition: " + from + " -> " + to);
        }
    }

    private static boolean isForwardTransition(TaskStatus from, TaskStatus to) {
        if (from == TaskStatus.OPEN) {
            return to == TaskStatus.IN_PROGRESS;
        }
        if (from == TaskStatus.IN_PROGRESS) {
            return to == TaskStatus.IN_REVIEW;
        }
        if (from == TaskStatus.IN_REVIEW) {
            return to == TaskStatus.DONE;
        }
        if (from == TaskStatus.DONE) {
            return to == TaskStatus.CLOSED;
        }
        return false;
    }
}
