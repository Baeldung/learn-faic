package com.baeldung.jiralite.task;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = {"project", "assignee", "reporter", "sprint"})
    @Query("select t from Task t"
            + " where (:projectIds is null or t.project.id in :projectIds)"
            + " and (:status is null or t.status = :status)"
            + " and (:priority is null or t.priority = :priority)"
            + " and (:assigneeId is null or t.assignee.id = :assigneeId)"
            + " and (:sprintId is null or t.sprint.id = :sprintId)"
            + " order by t.id")
    List<Task> findTasks(
            @Param("projectIds") List<Long> projectIds,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("assigneeId") Long assigneeId,
            @Param("sprintId") Long sprintId);

    @EntityGraph(attributePaths = {"project", "assignee", "reporter", "sprint"})
    @Query("select t from Task t where t.id = :id")
    Optional<Task> findByIdLoaded(@Param("id") Long id);
}
