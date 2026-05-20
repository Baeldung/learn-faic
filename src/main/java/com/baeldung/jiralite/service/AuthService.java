package com.baeldung.jiralite.service;

import com.baeldung.jiralite.domain.Role;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.dto.AuthResponse;
import com.baeldung.jiralite.dto.LoginRequest;
import com.baeldung.jiralite.dto.RegisterRequest;
import com.baeldung.jiralite.exception.ConflictException;
import com.baeldung.jiralite.repository.UserRepository;
import com.baeldung.jiralite.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ConflictException("Username already taken: " + req.username());
        }
        Role role = req.role() != null ? req.role() : Role.DEVELOPER;
        User user = new User(req.username(), passwordEncoder.encode(req.password()), role);
        userRepository.save(user);
        return new AuthResponse(jwtUtil.generate(user.getUsername()));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return new AuthResponse(jwtUtil.generate(user.getUsername()));
    }
}
