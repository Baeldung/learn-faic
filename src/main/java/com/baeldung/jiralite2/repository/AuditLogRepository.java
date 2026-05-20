package com.baeldung.jiralite2.repository;

import com.baeldung.jiralite2.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByProjectIdOrderByOccurredAtDesc(Long projectId);
    List<AuditLog> findAllByTaskIdOrderByOccurredAtDesc(Long taskId);
}
