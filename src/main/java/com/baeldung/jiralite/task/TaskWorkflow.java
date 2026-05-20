package com.baeldung.jiralite.task;

import com.baeldung.jiralite.user.Role;
import com.baeldung.jiralite.web.ConflictException;
import com.baeldung.jiralite.web.ForbiddenException;

final class TaskWorkflow {

    private TaskWorkflow() {
    }

    static void validate(TaskStatus from, TaskStatus to, Role actorRole) {
        if (from == to) {
            throw new ConflictException("Task is already in " + to);
        }
        if (to == TaskStatus.CLOSED) {
            requireManagerOrAdmin(actorRole, "transitioning to CLOSED");
            if (from != TaskStatus.DONE) {
                throw new ConflictException("Only DONE tasks can be CLOSED");
            }
            return;
        }
        if (from == TaskStatus.CLOSED) {
            if (to != TaskStatus.OPEN) {
                throw new ConflictException("CLOSED tasks can only be reopened to OPEN");
            }
            requireManagerOrAdmin(actorRole, "reopening a task");
            return;
        }
        if (!isForwardStep(from, to)) {
            throw new ConflictException("Invalid transition from " + from + " to " + to);
        }
    }

    private static boolean isForwardStep(TaskStatus from, TaskStatus to) {
        return nextOf(from) == to;
    }

    private static TaskStatus nextOf(TaskStatus status) {
        return switch (status) {
            case OPEN -> TaskStatus.IN_PROGRESS;
            case IN_PROGRESS -> TaskStatus.IN_REVIEW;
            case IN_REVIEW -> TaskStatus.DONE;
            default -> null;
        };
    }

    private static void requireManagerOrAdmin(Role role, String action) {
        if (role != Role.ADMIN && role != Role.MANAGER) {
            throw new ForbiddenException("Only MANAGER or ADMIN can perform " + action);
        }
    }
}
