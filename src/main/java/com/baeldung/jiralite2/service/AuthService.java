package com.baeldung.jiralite2.service;

import com.baeldung.jiralite2.domain.User;
import com.baeldung.jiralite2.domain.enums.Role;
import com.baeldung.jiralite2.dto.request.LoginRequest;
import com.baeldung.jiralite2.dto.request.RegisterRequest;
import com.baeldung.jiralite2.dto.response.AuthResponse;
import com.baeldung.jiralite2.exception.ConflictException;
import com.baeldung.jiralite2.repository.UserRepository;
import com.baeldung.jiralite2.security.JwtTokenProvider;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ConflictException("Username already taken: " + req.username());
        }
        User user = new User(req.username(), passwordEncoder.encode(req.password()), Role.DEVELOPER);
        userRepository.save(user);
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        String token = tokenProvider.generateToken(auth);
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        String token = tokenProvider.generateToken(auth);
        User user = userRepository.findByUsername(req.username()).orElseThrow();
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}
