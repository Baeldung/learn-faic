package com.baeldung.jiralite.sprint;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SprintRepository extends JpaRepository<Sprint, Long> {

    @Query("select s from Sprint s where s.project.id = :projectId order by s.id")
    List<Sprint> findByProjectId(@Param("projectId") Long projectId);
}
