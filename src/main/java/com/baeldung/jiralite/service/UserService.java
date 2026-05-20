package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.AuditEventType;
import com.baeldung.jiralite.domain.Role;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.dto.ChangeRoleRequest;
import com.baeldung.jiralite.dto.UserResponse;
import com.baeldung.jiralite.exception.ForbiddenException;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import com.baeldung.jiralite.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final String ENTITY_USER = "USER";

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse changeRole(Long userId, ChangeRoleRequest request, User actor) {
        if (actor.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can change roles");
        }
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(request.role());
        userRepo.save(user);
        auditLogService.log(AuditEventType.USER_ROLE_CHANGED, actor, ENTITY_USER, userId, null);
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
