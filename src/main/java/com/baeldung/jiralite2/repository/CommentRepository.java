package com.baeldung.jiralite2.repository;

import com.baeldung.jiralite2.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByTaskIdOrderByCreatedAtAsc(Long taskId);
}
