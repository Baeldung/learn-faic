package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.AuditEventType;
import com.baeldung.jiralite.domain.Role;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.dto.ChangeRoleRequest;
import com.baeldung.jiralite.dto.UserResponse;
import com.baeldung.jiralite.exception.ForbiddenException;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import com.baeldung.jiralite.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
            .map(UserResponse::from)
            .toList();
    }

    @Transactional
    public UserResponse changeRole(Long userId, ChangeRoleRequest req, User actor) {
        if (actor.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can change user roles");
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setRole(req.role());
        userRepository.save(user);
        auditLogService.log(AuditEventType.USER_ROLE_CHANGED, actor, "USER", userId, null);
        return UserResponse.from(user);
    }
}
