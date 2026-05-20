package com.baeldung.jiralite.repository;

import com.baeldung.jiralite.domain.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByProjectId(Long projectId);
    Optional<Sprint> findByIdAndProjectId(Long id, Long projectId);
}
