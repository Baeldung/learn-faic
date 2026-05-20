package com.baeldung.jiralite.task;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("select t from Task t where t.project.id = :projectId"
            + " and (:status is null or t.status = :status)"
            + " and (:assigneeId is null or t.assignee.id = :assigneeId)"
            + " and (:priority is null or t.priority = :priority)"
            + " and (:sprintId is null or t.sprint.id = :sprintId)")
    List<Task> findFiltered(
            @Param("projectId") Long projectId,
            @Param("status") TaskStatus status,
            @Param("assigneeId") Long assigneeId,
            @Param("priority") Priority priority,
            @Param("sprintId") Long sprintId);
}
