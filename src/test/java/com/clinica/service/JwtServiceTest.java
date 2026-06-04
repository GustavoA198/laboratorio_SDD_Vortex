package com.clinica.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService.
 */
class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET = "testSecretKeyForUnitTestingPurposesOnly123456789012345678901234567890";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
    }

    @Nested
    @DisplayName("createToken")
    class CreateToken {

        @Test
        @DisplayName("should create token with correct username and rol claims")
        void shouldCreateTokenWithCorrectClaims() {
            String username = "testuser";
            String rol = "PACIENTE";

            String token = jwtService.createToken(username, rol);

            assertNotNull(token);
            assertFalse(token.isEmpty());

            Claims claims = jwtService.getClaims(token);
            assertEquals(username, claims.getSubject());
            assertEquals(rol, claims.get("rol", String.class));
        }

        @Test
        @DisplayName("should create token with different roles")
        void shouldCreateTokenWithDifferentRoles() {
            String username = "doctor1";
            String rol = "MEDICO";

            String token = jwtService.createToken(username, rol);
            Claims claims = jwtService.getClaims(token);

            assertEquals(rol, claims.get("rol", String.class));
        }
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateToken {

        @Test
        @DisplayName("should return true for valid token")
        void shouldReturnTrueForValidToken() {
            String token = jwtService.createToken("user", "PACIENTE");

            assertTrue(jwtService.validateToken(token));
        }

        @Test
        @DisplayName("should return false for invalid token")
        void shouldReturnFalseForInvalidToken() {
            assertFalse(jwtService.validateToken("invalid.token.here"));
        }

        @Test
        @DisplayName("should return false for null token")
        void shouldReturnFalseForNullToken() {
            assertFalse(jwtService.validateToken(null));
        }

        @Test
        @DisplayName("should return false for empty token")
        void shouldReturnFalseForEmptyToken() {
            assertFalse(jwtService.validateToken(""));
        }

        @Test
        @DisplayName("should return false for tampered token")
        void shouldReturnFalseForTamperedToken() {
            String token = jwtService.createToken("user", "PACIENTE");
            String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

            assertFalse(jwtService.validateToken(tamperedToken));
        }
    }

    @Nested
    @DisplayName("getClaims")
    class GetClaims {

        @Test
        @DisplayName("should extract claims from valid token")
        void shouldExtractClaimsFromValidToken() {
            String username = "admin";
            String rol = "ADMIN";
            String token = jwtService.createToken(username, rol);

            Claims claims = jwtService.getClaims(token);

            assertEquals(username, claims.getSubject());
            assertEquals(rol, claims.get("rol", String.class));
            assertNotNull(claims.getIssuedAt());
            assertNotNull(claims.getExpiration());
        }

        @Test
        @DisplayName("should throw JwtException for malformed token")
        void shouldThrowForMalformedToken() {
            assertThrows(JwtException.class, () -> jwtService.getClaims("not.a.valid.token"));
        }

        @Test
        @DisplayName("should throw exception for empty token")
        void shouldThrowForEmptyToken() {
            assertThrows(Exception.class, () -> jwtService.getClaims(""));
        }
    }

    @Nested
    @DisplayName("getUsernameFromToken")
    class GetUsernameFromToken {

        @Test
        @DisplayName("should extract username from token")
        void shouldExtractUsernameFromToken() {
            String username = "paciente123";
            String token = jwtService.createToken(username, "PACIENTE");

            assertEquals(username, jwtService.getUsernameFromToken(token));
        }
    }

    @Nested
    @DisplayName("getRolFromToken")
    class GetRolFromToken {

        @Test
        @DisplayName("should extract rol from token")
        void shouldExtractRolFromToken() {
            String rol = "MEDICO";
            String token = jwtService.createToken("doctor", rol);

            assertEquals(rol, jwtService.getRolFromToken(token));
        }
    }
}