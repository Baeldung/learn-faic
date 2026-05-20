package com.baeldung.jiralite.repository;

import com.baeldung.jiralite.domain.Task;
import com.baeldung.jiralite.domain.TaskPriority;
import com.baeldung.jiralite.domain.TaskStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    @Query("SELECT t FROM Task t WHERE "
        + "(:projectId IS NULL OR t.project.id = :projectId) AND "
        + "(:status IS NULL OR t.status = :status) AND "
        + "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND "
        + "(:priority IS NULL OR t.priority = :priority) AND "
        + "(:sprintId IS NULL OR t.sprint.id = :sprintId)")
    List<Task> findAllByFilters(
        @Param("projectId") Long projectId,
        @Param("status") TaskStatus status,
        @Param("assigneeId") Long assigneeId,
        @Param("priority") TaskPriority priority,
        @Param("sprintId") Long sprintId);
}
