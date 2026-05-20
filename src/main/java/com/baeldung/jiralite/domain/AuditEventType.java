package com.baeldung.jiralite.domain;

public enum AuditEventType {
    PROJECT_CREATED,
    TASK_CREATED,
    TASK_STATUS_CHANGED,
    SPRINT_CREATED,
    SPRINT_STARTED,
    SPRINT_COMPLETED,
    COMMENT_ADDED,
    ROLE_CHANGED,
    MEMBER_ADDED,
    MEMBER_REMOVED
}
