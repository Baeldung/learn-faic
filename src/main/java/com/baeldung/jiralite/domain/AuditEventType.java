package com.baeldung.jiralite.domain;

public enum AuditEventType {
    PROJECT_CREATED,
    MEMBER_ADDED,
    MEMBER_REMOVED,
    SPRINT_CREATED,
    SPRINT_STARTED,
    SPRINT_COMPLETED,
    TASK_CREATED,
    TASK_STATUS_CHANGED,
    COMMENT_ADDED,
    USER_ROLE_CHANGED
}
