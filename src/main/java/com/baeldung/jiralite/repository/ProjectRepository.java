package com.baeldung.jiralite.repository;

import com.baeldung.jiralite.domain.Project;
import com.baeldung.jiralite.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p JOIN p.members m WHERE m = :user")
    List<Project> findByMember(@Param("user") User user);

    @Query("SELECT p FROM Project p JOIN FETCH p.members WHERE p.id = :id")
    Optional<Project> findByIdWithMembers(@Param("id") Long id);

    @Query("SELECT COUNT(p) > 0 FROM Project p JOIN p.members m WHERE p.id = :projectId AND m.id = :userId")
    boolean isMember(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
