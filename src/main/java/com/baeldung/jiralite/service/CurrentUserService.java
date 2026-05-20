package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.repository.UserRepository;
import com.baeldung.jiralite.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        return userRepository.findById(principal.getId())
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }
}
