package com.baeldung.jiralite.project;

import com.baeldung.jiralite.user.User;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(Project project) {
        List<Long> memberIds = project.getMembers().stream().map(User::getId).sorted().toList();
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), memberIds);
    }
}
