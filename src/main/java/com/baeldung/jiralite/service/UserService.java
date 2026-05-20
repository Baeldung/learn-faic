package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.AuditEventType;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.dto.UserResponse;
import com.baeldung.jiralite.exception.ResourceNotFoundException;
import com.baeldung.jiralite.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final CurrentUserService currentUserService;

    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository,
            CurrentUserService currentUserService,
            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
            .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getRole()))
            .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse changeRole(Long targetUserId, com.baeldung.jiralite.domain.Role newRole) {
        User caller = currentUserService.getCurrentUser();
        RoleChecker.requireAdmin(caller);
        User target = userRepository.findById(targetUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + targetUserId));
        target.setRole(newRole);
        userRepository.save(target);
        auditLogService.record(AuditEventType.ROLE_CHANGED, caller, null, null,
            "User " + target.getUsername() + " role changed to " + newRole);
        return new UserResponse(target.getId(), target.getUsername(), target.getRole());
    }
}
