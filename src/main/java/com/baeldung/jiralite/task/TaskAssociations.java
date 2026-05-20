package com.baeldung.jiralite.task;

import com.baeldung.jiralite.sprint.Sprint;
import com.baeldung.jiralite.sprint.SprintRepository;
import com.baeldung.jiralite.user.User;
import com.baeldung.jiralite.user.UserRepository;
import com.baeldung.jiralite.web.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class TaskAssociations {

    private final UserRepository userRepository;
    private final SprintRepository sprintRepository;

    public TaskAssociations(UserRepository userRepository, SprintRepository sprintRepository) {
        this.userRepository = userRepository;
        this.sprintRepository = sprintRepository;
    }

    public void apply(Task task, Long assigneeId, Long sprintId) {
        applyAssignee(task, assigneeId);
        applySprint(task, sprintId);
    }

    private void applyAssignee(Task task, Long assigneeId) {
        if (assigneeId == null) {
            task.setAssignee(null);
            return;
        }
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ValidationException("Assignee " + assigneeId + " not found"));
        if (!task.getProject().hasMember(assignee.getId())) {
            throw new ValidationException("Assignee " + assigneeId + " is not a member of the project");
        }
        task.setAssignee(assignee);
    }

    private void applySprint(Task task, Long sprintId) {
        if (sprintId == null) {
            task.setSprint(null);
            return;
        }
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ValidationException("Sprint " + sprintId + " not found"));
        if (!sprint.getProject().getId().equals(task.getProject().getId())) {
            throw new ValidationException("Sprint " + sprintId + " is not in this project");
        }
        task.setSprint(sprint);
    }
}
