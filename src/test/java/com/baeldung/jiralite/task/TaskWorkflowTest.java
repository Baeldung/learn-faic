package com.baeldung.jiralite.task;

import com.baeldung.jiralite.user.Role;
import com.baeldung.jiralite.web.ConflictException;
import com.baeldung.jiralite.web.ForbiddenException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskWorkflowTest {

    @Test
    void forwardStepsAreAllowed() {
        assertDoesNotThrow(() -> TaskWorkflow.validate(TaskStatus.OPEN, TaskStatus.IN_PROGRESS, Role.DEVELOPER));
        assertDoesNotThrow(() -> TaskWorkflow.validate(TaskStatus.IN_PROGRESS, TaskStatus.IN_REVIEW, Role.DEVELOPER));
        assertDoesNotThrow(() -> TaskWorkflow.validate(TaskStatus.IN_REVIEW, TaskStatus.DONE, Role.DEVELOPER));
    }

    @Test
    void skippingForwardStateIsRejected() {
        assertThrows(ConflictException.class,
                () -> TaskWorkflow.validate(TaskStatus.OPEN, TaskStatus.DONE, Role.MANAGER));
        assertThrows(ConflictException.class,
                () -> TaskWorkflow.validate(TaskStatus.IN_PROGRESS, TaskStatus.CLOSED, Role.MANAGER));
    }

    @Test
    void onlyManagerOrAdminCanClose() {
        assertDoesNotThrow(() -> TaskWorkflow.validate(TaskStatus.DONE, TaskStatus.CLOSED, Role.MANAGER));
        assertDoesNotThrow(() -> TaskWorkflow.validate(TaskStatus.DONE, TaskStatus.CLOSED, Role.ADMIN));
        assertThrows(ForbiddenException.class,
                () -> TaskWorkflow.validate(TaskStatus.DONE, TaskStatus.CLOSED, Role.DEVELOPER));
    }

    @Test
    void closeRequiresDoneSource() {
        assertThrows(ConflictException.class,
                () -> TaskWorkflow.validate(TaskStatus.IN_REVIEW, TaskStatus.CLOSED, Role.MANAGER));
    }

    @Test
    void reopenIsManagerOnly() {
        assertDoesNotThrow(() -> TaskWorkflow.validate(TaskStatus.CLOSED, TaskStatus.OPEN, Role.MANAGER));
        assertThrows(ForbiddenException.class,
                () -> TaskWorkflow.validate(TaskStatus.CLOSED, TaskStatus.OPEN, Role.DEVELOPER));
    }

    @Test
    void reopenMustGoToOpen() {
        assertThrows(ConflictException.class,
                () -> TaskWorkflow.validate(TaskStatus.CLOSED, TaskStatus.IN_PROGRESS, Role.MANAGER));
    }

    @Test
    void backwardsTransitionRejected() {
        assertThrows(ConflictException.class,
                () -> TaskWorkflow.validate(TaskStatus.IN_PROGRESS, TaskStatus.OPEN, Role.MANAGER));
    }

    @Test
    void noOpRejected() {
        assertThrows(ConflictException.class,
                () -> TaskWorkflow.validate(TaskStatus.OPEN, TaskStatus.OPEN, Role.DEVELOPER));
    }
}
