package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.Role;
import com.baeldung.jiralite.domain.Task;
import com.baeldung.jiralite.domain.TaskStatus;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.exception.ForbiddenException;
import com.baeldung.jiralite.exception.InvalidTransitionException;
import org.springframework.stereotype.Service;

@Service
public class TaskTransitionValidator {

    public void validate(Task task, TaskStatus to, User actor) {
        TaskStatus from = task.getStatus();
        if (!from.canTransitionTo(to)) {
            throw new InvalidTransitionException(from, to);
        }
        boolean restricted = to == TaskStatus.CLOSED || from == TaskStatus.CLOSED;
        if (restricted && actor.getRole() != Role.MANAGER && actor.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only Managers and Admins can close or reopen tasks");
        }
    }
}
