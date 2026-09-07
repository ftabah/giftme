package com.ftabah.giftme.adapter.security;

import com.ftabah.giftme.application.port.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenService implements TokenService {

    private final JwtProperties properties;
    private final SecretKey key;
    private final TokenRevocationStore revocations;

    public JwtTokenService(JwtProperties properties, TokenRevocationStore revocations) {
        this.properties = properties;
        this.revocations = revocations;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String create(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(properties.expirationSeconds())))
                .signWith(key)
                .compact();
    }

    @Override
    public UUID parseUserId(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return UUID.fromString(claims.getSubject());
    }

    @Override
    public boolean isRevoked(String token) {
        return revocations.isRevoked(token);
    }

    @Override
    public void revoke(String token) {
        revocations.revoke(token);
    }
}