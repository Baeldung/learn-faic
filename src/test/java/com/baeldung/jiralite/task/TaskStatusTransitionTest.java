package com.baeldung.jiralite.task;

import org.junit.jupiter.api.Test;

import com.baeldung.jiralite.web.ConflictException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskStatusTransitionTest {

    @Test
    void openToInProgressIsValid() {
        assertThatCode(() -> TaskStatus.OPEN.validateTransitionTo(TaskStatus.IN_PROGRESS))
                .doesNotThrowAnyException();
    }

    @Test
    void inProgressToInReviewIsValid() {
        assertThatCode(() -> TaskStatus.IN_PROGRESS.validateTransitionTo(TaskStatus.IN_REVIEW))
                .doesNotThrowAnyException();
    }

    @Test
    void inReviewToDoneIsValid() {
        assertThatCode(() -> TaskStatus.IN_REVIEW.validateTransitionTo(TaskStatus.DONE))
                .doesNotThrowAnyException();
    }

    @Test
    void doneToClosedIsValid() {
        assertThatCode(() -> TaskStatus.DONE.validateTransitionTo(TaskStatus.CLOSED))
                .doesNotThrowAnyException();
    }

    @Test
    void closedToOpenIsValid() {
        assertThatCode(() -> TaskStatus.CLOSED.validateTransitionTo(TaskStatus.OPEN))
                .doesNotThrowAnyException();
    }

    @Test
    void transitionFromOpenToDoneIsRejected() {
        assertThatThrownBy(() -> TaskStatus.OPEN.validateTransitionTo(TaskStatus.DONE))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void transitionFromInProgressToClosedIsRejected() {
        assertThatThrownBy(() -> TaskStatus.IN_PROGRESS.validateTransitionTo(TaskStatus.CLOSED))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void transitionFromClosedToInProgressIsRejected() {
        assertThatThrownBy(() -> TaskStatus.CLOSED.validateTransitionTo(TaskStatus.IN_PROGRESS))
                .isInstanceOf(ConflictException.class);
    }
}
