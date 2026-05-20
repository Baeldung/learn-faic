package com.baeldung.jiralite.repository;

import com.baeldung.jiralite.domain.AuditLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<AuditLog> findByTaskIdOrderByCreatedAtDesc(Long taskId);
}
