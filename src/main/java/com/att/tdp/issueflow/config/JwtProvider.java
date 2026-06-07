package com.att.tdp.issueflow.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
/**
 * Role: Handles the generation, validation, and parsing of JSON Web Tokens
 * (JWT).
 * It uses a secret key configured in the application properties to
 * cryptographically sign and verify the tokens.
 */
public class JwtProvider {

    private final SecretKey key;
    private final long expirationMs;

    // Constructs a JwtProvider instance, initializing the cryptographic key from
    // the provided secret.
    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // Generates a new JWT token for the user with the specified username as the
    // subject.
    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    // Parses the JWT token to extract and return the username (subject).
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // Returns true if the token is signed correctly and not expired.
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Returns expiration in seconds (for the login response expiresIn field).
    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    // Parses and verifies the JWT token, returning its claims payload.
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
