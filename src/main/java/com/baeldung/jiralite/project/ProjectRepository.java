package com.baeldung.jiralite.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("select count(p) > 0 from Project p join p.members m where p.id = :projectId and m.id = :userId")
    boolean isMember(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
