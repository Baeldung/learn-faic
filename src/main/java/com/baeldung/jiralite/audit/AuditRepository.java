package com.baeldung.jiralite.audit;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditRepository extends JpaRepository<AuditEntry, Long> {

    @EntityGraph(attributePaths = {"actor"})
    @Query("select a from AuditEntry a where a.projectId = :projectId order by a.timestamp desc")
    List<AuditEntry> findByProject(@Param("projectId") Long projectId);

    @EntityGraph(attributePaths = {"actor"})
    @Query("select a from AuditEntry a where a.entityType = com.baeldung.jiralite.audit.AuditEntityType.TASK"
            + " and a.entityId = :taskId order by a.timestamp desc")
    List<AuditEntry> findByTask(@Param("taskId") Long taskId);
}
