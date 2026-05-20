package com.baeldung.jiralite.user;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baeldung.jiralite.audit.AuditEventType;
import com.baeldung.jiralite.audit.AuditService;
import com.baeldung.jiralite.web.ConflictException;
import com.baeldung.jiralite.web.NotFoundException;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final AuditService auditService;

    public UserService(UserRepository userRepository, UserMapper userMapper,
            PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public User register(String username, String password, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already taken: " + username);
        }
        Role effective = role != null ? role : Role.DEVELOPER;
        User user = new User(username, passwordEncoder.encode(password), effective);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse changeRole(Long userId, ChangeRoleRequest request, Long actorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
        user.setRole(request.role());
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new NotFoundException("Actor " + actorId + " not found"));
        auditService.record(AuditEventType.ROLE_CHANGED, actor, null, null);
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
    }
}
