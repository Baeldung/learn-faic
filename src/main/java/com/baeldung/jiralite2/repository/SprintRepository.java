package com.baeldung.jiralite2.repository;

import com.baeldung.jiralite2.domain.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findAllByProjectId(Long projectId);
}
