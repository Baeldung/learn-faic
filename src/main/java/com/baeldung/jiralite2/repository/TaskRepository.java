package com.baeldung.jiralite2.repository;

import com.baeldung.jiralite2.domain.Task;
import com.baeldung.jiralite2.domain.enums.Priority;
import com.baeldung.jiralite2.domain.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
        SELECT t FROM Task t
        WHERE (:projectId IS NULL OR t.project.id = :projectId)
          AND (:status IS NULL OR t.status = :status)
          AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
          AND (:priority IS NULL OR t.priority = :priority)
          AND (:sprintId IS NULL OR t.sprint.id = :sprintId)
        """)
    List<Task> findWithFilters(
        @Param("projectId") Long projectId,
        @Param("status") TaskStatus status,
        @Param("assigneeId") Long assigneeId,
        @Param("priority") Priority priority,
        @Param("sprintId") Long sprintId
    );
}
