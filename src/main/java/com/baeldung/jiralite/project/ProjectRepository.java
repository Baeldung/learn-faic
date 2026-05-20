package com.baeldung.jiralite.project;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @EntityGraph(attributePaths = {"members"})
    @Query("select distinct p from Project p left join p.members m where m.id = :userId")
    List<Project> findAllForMember(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"members"})
    @Query("select p from Project p")
    List<Project> findAllWithMembers();

    @EntityGraph(attributePaths = {"members"})
    @Query("select p from Project p where p.id = :id")
    Optional<Project> findByIdWithMembers(@Param("id") Long id);
}
