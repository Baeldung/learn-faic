package com.baeldung.jiralite.user;

import com.baeldung.jiralite.audit.AuditEntityType;
import com.baeldung.jiralite.audit.AuditEventType;
import com.baeldung.jiralite.audit.AuditLogger;
import com.baeldung.jiralite.audit.AuditWrite;
import com.baeldung.jiralite.web.ConflictException;
import com.baeldung.jiralite.web.NotFoundException;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuditLogger audit;

    public UserService(UserRepository userRepository, UserMapper userMapper, AuditLogger audit) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.audit = audit;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse changeRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
        if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN
                && audit.currentId().equals(user.getId())
                && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new ConflictException("Cannot demote the last ADMIN");
        }
        Role oldRole = user.getRole();
        user.setRole(newRole);
        audit.log(new AuditWrite(AuditEventType.USER_ROLE_CHANGED, AuditEntityType.USER, user.getId(), null,
                oldRole + " -> " + newRole));
        return userMapper.toResponse(user);
    }
}
