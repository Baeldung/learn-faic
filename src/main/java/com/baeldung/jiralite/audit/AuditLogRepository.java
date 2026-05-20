package com.baeldung.jiralite.audit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByProjectIdOrderByTimestampDesc(@Param("projectId") Long projectId);

    List<AuditLog> findByTaskIdOrderByTimestampDesc(@Param("taskId") Long taskId);
}
