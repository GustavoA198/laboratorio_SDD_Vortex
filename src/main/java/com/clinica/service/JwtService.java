package com.clinica.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Service for JWT token creation and validation.
 */
@Service
public class JwtService {

    private static final long EXPIRATION_MS = 20 * 60 * 1000; // 20 minutes

    @Value("${jwt.secret:clinica-dev-secret-key-2024}")
    private String secretKey;

    /**
     * Creates a JWT token with the given username and role.
     *
     * @param username the username (subject)
     * @param rol the role (PACIENTE, MEDICO, ADMIN)
     * @return the signed JWT token
     */
    public String createToken(String username, String rol) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validates a JWT token.
     *
     * @param token the token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extracts claims from a JWT token.
     *
     * @param token the token
     * @return the claims
     * @throws JwtException if token is invalid
     */
    public Claims getClaims(String token) throws JwtException {
        return parseToken(token);
    }

    /**
     * Extracts username from a JWT token.
     *
     * @param token the token
     * @return the username
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * Extracts role from a JWT token.
     *
     * @param token the token
     * @return the role
     */
    public String getRolFromToken(String token) {
        return parseToken(token).get("rol", String.class);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}