package com.baeldung.jiralite2.repository;

import com.baeldung.jiralite2.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByMembersId(Long userId);
}
