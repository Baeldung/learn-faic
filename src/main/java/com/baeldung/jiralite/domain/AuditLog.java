package com.baeldung.jiralite.domain;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public AuditLog() {}

    public AuditLog(AuditEventType eventType, User actor, String entityType, Long entityId, Project project) {
        this.eventType = eventType;
        this.actor = actor;
        this.entityType = entityType;
        this.entityId = entityId;
        this.project = project;
    }

    public Long getId() { return id; }
    public AuditEventType getEventType() { return eventType; }
    public User getActor() { return actor; }
    public String getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
    public Project getProject() { return project; }
    public Instant getCreatedAt() { return createdAt; }
}
