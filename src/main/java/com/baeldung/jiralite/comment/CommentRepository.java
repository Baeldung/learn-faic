package com.baeldung.jiralite.comment;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"author"})
    @Query("select c from Comment c where c.task.id = :taskId order by c.createdAt")
    List<Comment> findByTaskId(@Param("taskId") Long taskId);
}
