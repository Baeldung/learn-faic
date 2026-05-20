package com.baeldung.jiralite.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "task_id")
    private Long taskId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(length = 2000)
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AuditLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long projectId;
        private Long taskId;
        private String action;
        private Long actorId;
        private String details;

        public Builder projectId(Long projectId) { this.projectId = projectId; return this; }
        public Builder taskId(Long taskId) { this.taskId = taskId; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder actorId(Long actorId) { this.actorId = actorId; return this; }
        public Builder details(String details) { this.details = details; return this; }
        public AuditLog build() {
            AuditLog a = new AuditLog();
            a.projectId = projectId;
            a.taskId = taskId;
            a.action = action;
            a.actorId = actorId;
            a.details = details;
            return a;
        }
    }
}
