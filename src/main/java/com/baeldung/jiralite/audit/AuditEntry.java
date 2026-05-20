package com.baeldung.jiralite.audit;

import com.baeldung.jiralite.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_entries", indexes = {
        @Index(name = "idx_audit_project", columnList = "project_id"),
        @Index(name = "idx_audit_entity", columnList = "entityType,entityId")
})
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditEventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditEntityType entityType;

    @Column(nullable = false)
    private Long entityId;

    @Column(name = "project_id")
    private Long projectId;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(length = 1000)
    private String details;

    protected AuditEntry() {
    }

    public AuditEntry(User actor, AuditWrite write) {
        this.actor = actor;
        this.eventType = write.eventType();
        this.entityType = write.entityType();
        this.entityId = write.entityId();
        this.projectId = write.projectId();
        this.details = write.details();
        this.timestamp = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public User getActor() {
        return actor;
    }

    public AuditEntityType getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getDetails() {
        return details;
    }
}
