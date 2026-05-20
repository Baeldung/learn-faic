package com.baeldung.jiralite.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.baeldung.jiralite.user.User;
import com.baeldung.jiralite.user.UserRepository;
import com.baeldung.jiralite.web.UnauthorizedException;

@Component
public class CurrentUser {

    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof AppUserDetails details)) {
            throw new UnauthorizedException("Not authenticated");
        }
        return userRepository.findById(details.getId())
                .orElseThrow(() -> new UnauthorizedException("User no longer exists"));
    }

    public Long getId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppUserDetails details)) {
            throw new UnauthorizedException("Not authenticated");
        }
        return details.getId();
    }
}
