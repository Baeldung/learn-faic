package com.baeldung.jiralite2.domain;

import com.baeldung.jiralite2.domain.enums.AuditEventType;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditEventType eventType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "actor_id")
    private User actor;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    // nullable — not all events are task-scoped
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    private String detail;

    @Column(nullable = false)
    private Instant occurredAt = Instant.now();

    public AuditLog() {}

    public Long getId() { return id; }
    public AuditEventType getEventType() { return eventType; }
    public void setEventType(AuditEventType eventType) { this.eventType = eventType; }
    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public Instant getOccurredAt() { return occurredAt; }
}
