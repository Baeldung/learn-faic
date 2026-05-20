package com.baeldung.jiralite.security;

import com.baeldung.jiralite.user.Role;
import com.baeldung.jiralite.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_ROLE = "role";
    private static final int SECONDS_PER_MINUTE = 60;

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(@Value("${jiralite.jwt.secret}") String secret,
                      @Value("${jiralite.jwt.expiration-minutes}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String issue(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_ROLE, user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationMinutes * SECONDS_PER_MINUTE)))
                .signWith(key)
                .compact();
    }

    public JwtPrincipal parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long userId = claims.get(CLAIM_USER_ID, Long.class);
        String role = claims.get(CLAIM_ROLE, String.class);
        return new JwtPrincipal(userId, claims.getSubject(), Role.valueOf(role));
    }
}
