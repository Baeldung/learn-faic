package com.baeldung.jiralite.repository;

import com.baeldung.jiralite.domain.Task;
import com.baeldung.jiralite.domain.enums.TaskPriority;
import com.baeldung.jiralite.domain.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndProjectId(Long id, Long projectId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:sprintId = -1 OR (:sprintId = 0 AND t.sprint IS NULL) OR t.sprint.id = :sprintId)")
    List<Task> findWithFilters(
        @Param("projectId") Long projectId,
        @Param("status") TaskStatus status,
        @Param("assigneeId") Long assigneeId,
        @Param("priority") TaskPriority priority,
        @Param("sprintId") Long sprintId
    );
}
