package com.baeldung.jiralite.repository;

import com.baeldung.jiralite.domain.Sprint;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SprintRepository extends JpaRepository<Sprint, Long> {

    List<Sprint> findByProjectId(Long projectId);
}
