package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.enums.Role;
import com.baeldung.jiralite.domain.enums.TaskStatus;
import com.baeldung.jiralite.exception.ForbiddenException;
import com.baeldung.jiralite.exception.InvalidTransitionException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class TaskTransitionValidator {

    private static final Map<TaskStatus, Set<TaskStatus>> ALLOWED_TRANSITIONS = Map.of(
        TaskStatus.OPEN,        Set.of(TaskStatus.IN_PROGRESS),
        TaskStatus.IN_PROGRESS, Set.of(TaskStatus.IN_REVIEW),
        TaskStatus.IN_REVIEW,   Set.of(TaskStatus.DONE),
        TaskStatus.DONE,        Set.of(TaskStatus.CLOSED),
        TaskStatus.CLOSED,      Set.of(TaskStatus.OPEN)
    );

    private static final Set<TaskStatus> MANAGER_ONLY_TARGETS = Set.of(TaskStatus.CLOSED, TaskStatus.OPEN);

    public void validate(TaskStatus from, TaskStatus to, Role actorRole) {
        Set<TaskStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new InvalidTransitionException(
                    String.format("Invalid transition from %s to %s. Allowed targets from %s: %s",
                            from, to, from, allowed));
        }
        if (MANAGER_ONLY_TARGETS.contains(to) && actorRole != Role.ADMIN && actorRole != Role.MANAGER) {
            throw new ForbiddenException("Only ADMIN or MANAGER can transition to " + to);
        }
    }
}
