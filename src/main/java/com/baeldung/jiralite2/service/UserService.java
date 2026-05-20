package com.baeldung.jiralite2.service;

import com.baeldung.jiralite2.domain.User;
import com.baeldung.jiralite2.domain.enums.AuditEventType;
import com.baeldung.jiralite2.domain.enums.Role;
import com.baeldung.jiralite2.dto.request.ChangeRoleRequest;
import com.baeldung.jiralite2.dto.response.UserResponse;
import com.baeldung.jiralite2.exception.AccessDeniedException;
import com.baeldung.jiralite2.exception.ResourceNotFoundException;
import com.baeldung.jiralite2.repository.UserRepository;
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
    public List<UserResponse> listAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse changeRole(Long targetId, ChangeRoleRequest req, String actorUsername) {
        User actor = loadByUsername(actorUsername);
        if (actor.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only ADMINs can change roles");
        }
        User target = userRepository.findById(targetId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + targetId));
        Role previous = target.getRole();
        target.setRole(req.role());
        userRepository.save(target);
        auditLogService.record(AuditEventType.ROLE_CHANGED, actor, null, null,
            target.getUsername() + " role changed from " + previous + " to " + req.role());
        return UserResponse.from(target);
    }

    public User loadByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public User loadById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
