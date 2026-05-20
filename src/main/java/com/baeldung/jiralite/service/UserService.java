package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.dto.ChangeRoleRequest;
import com.baeldung.jiralite.dto.UserResponse;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import com.baeldung.jiralite.repository.UserRepository;
import com.baeldung.jiralite.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository, SecurityUtils securityUtils,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return UserResponse.from(findUser(id));
    }

    @Transactional
    public UserResponse changeRole(Long userId, ChangeRoleRequest request) {
        User actor = securityUtils.getCurrentUser();
        User user = findUser(userId);
        String oldRole = user.getRole().name();
        user.setRole(request.getRole());
        userRepository.save(user);
        auditLogService.log("USER_ROLE_CHANGED", actor.getId(), null, null,
                String.format("User %s role changed from %s to %s", user.getUsername(), oldRole, request.getRole()));
        return UserResponse.from(user);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
