package com.baeldung.jiralite.auth;

import com.baeldung.jiralite.security.JwtService;
import com.baeldung.jiralite.user.Role;
import com.baeldung.jiralite.user.User;
import com.baeldung.jiralite.user.UserMapper;
import com.baeldung.jiralite.user.UserRepository;
import com.baeldung.jiralite.user.UserResponse;
import com.baeldung.jiralite.web.ConflictException;
import com.baeldung.jiralite.web.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken");
        }
        User user = new User(request.username(), passwordEncoder.encode(request.password()), Role.DEVELOPER);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return new LoginResponse(jwtService.issue(user));
    }
}
