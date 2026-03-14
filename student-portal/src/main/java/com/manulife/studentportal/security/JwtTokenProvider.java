package com.manulife.studentportal.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getUserId())
                .claim("role", userDetails.getRole())
                .claim("profileId", userDetails.getProfileId())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String generateToken(String username, Long userId, String role, Long profileId, String jti) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .id(jti)
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("profileId", profileId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }

    public String getTokenId(String token) {
        return parseToken(token).getId();
    }

    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public Long getProfileId(String token) {
        return parseToken(token).get("profileId", Long.class);
    }

    public Date getExpiration(String token) {
        return parseToken(token).getExpiration();
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public long getExpirationInSeconds() {
        return expirationMs / 1000;
    }
}