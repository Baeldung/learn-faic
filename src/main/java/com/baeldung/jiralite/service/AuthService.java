package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.Role;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.dto.AuthResponse;
import com.baeldung.jiralite.dto.LoginRequest;
import com.baeldung.jiralite.dto.RegisterRequest;
import com.baeldung.jiralite.exception.ConflictException;
import com.baeldung.jiralite.repository.UserRepository;
import com.baeldung.jiralite.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepo.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken: " + request.username());
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.DEVELOPER);
        userRepo.save(user);
        return new AuthResponse(jwtUtil.generate(user.getUsername()));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepo.findByUsername(request.username())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return new AuthResponse(jwtUtil.generate(user.getUsername()));
    }
}
