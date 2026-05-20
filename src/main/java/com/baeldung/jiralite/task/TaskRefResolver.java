package com.baeldung.jiralite.task;

import org.springframework.stereotype.Component;

import com.baeldung.jiralite.project.Project;
import com.baeldung.jiralite.project.ProjectService;
import com.baeldung.jiralite.sprint.Sprint;
import com.baeldung.jiralite.sprint.SprintService;
import com.baeldung.jiralite.user.User;
import com.baeldung.jiralite.user.UserRepository;
import com.baeldung.jiralite.web.NotFoundException;

@Component
public class TaskRefResolver {

    private final UserRepository userRepository;
    private final SprintService sprintService;
    private final ProjectService projectService;

    public TaskRefResolver(UserRepository userRepository, SprintService sprintService, ProjectService projectService) {
        this.userRepository = userRepository;
        this.sprintService = sprintService;
        this.projectService = projectService;
    }

    public User resolveUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
    }

    public Sprint resolveSprint(Long sprintId) {
        return sprintService.findEntity(sprintId);
    }

    public Project requireProjectAccess(Long projectId, Long userId) {
        Project project = projectService.findEntity(projectId);
        projectService.requireMemberOrAdmin(project, userId);
        return project;
    }

    public void requireProjectAccess(Project project, Long userId) {
        projectService.requireMemberOrAdmin(project, userId);
    }
}
