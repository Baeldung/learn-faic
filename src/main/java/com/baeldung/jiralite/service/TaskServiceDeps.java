package com.baeldung.jiralite.service;

import com.baeldung.jiralite.repository.ProjectRepository;
import com.baeldung.jiralite.repository.SprintRepository;
import com.baeldung.jiralite.repository.TaskRepository;
import com.baeldung.jiralite.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class TaskServiceDeps {

    private final TaskRepository taskRepository;

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final SprintRepository sprintRepository;

    public TaskServiceDeps(TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            SprintRepository sprintRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.sprintRepository = sprintRepository;
    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    public ProjectRepository getProjectRepository() {
        return projectRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public SprintRepository getSprintRepository() {
        return sprintRepository;
    }
}
