package com.baeldung.jiralite.audit;

import com.baeldung.jiralite.security.CurrentUser;
import com.baeldung.jiralite.user.Role;
import com.baeldung.jiralite.user.User;
import com.baeldung.jiralite.user.UserRepository;
import com.baeldung.jiralite.web.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuditLogger {

    private final AuditRepository auditRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public AuditLogger(AuditRepository auditRepository, UserRepository userRepository, CurrentUser currentUser) {
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    @Transactional
    public void log(AuditWrite write) {
        auditRepository.save(new AuditEntry(currentEntity(), write));
    }

    public User currentEntity() {
        Long id = currentUser.id();
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
    }

    public Long currentId() {
        return currentUser.id();
    }

    public Role currentRole() {
        return currentUser.role();
    }

    public boolean isAdmin() {
        return currentUser.isAdmin();
    }
}
