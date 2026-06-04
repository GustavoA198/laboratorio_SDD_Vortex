package com.clinica.service;

import com.clinica.dto.AuthResponse;
import com.clinica.dto.LoginRequest;
import com.clinica.model.Usuario;
import com.clinica.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private JwtService jwtService;
    private AuthService authService;

    private static final String TEST_SECRET = "testSecretKeyForUnitTestingPurposesOnly123456789012345678901234567890";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        authService = new AuthService(usuarioRepository, jwtService);
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("should return token on valid credentials")
        void shouldReturnTokenOnValidCredentials() {
            String username = "paciente1";
            String password = "password123";
            String rol = "PACIENTE";

            Usuario usuario = Usuario.builder()
                    .id(username)
                    .username(username)
                    .password(password)
                    .rol(rol)
                    .build();

            when(usuarioRepository.findByUsername(username)).thenReturn(Optional.of(usuario));

            LoginRequest request = new LoginRequest(username, password);
            AuthResponse response = authService.login(request);

            assertNotNull(response);
            assertNotNull(response.token());
            assertTrue(response.token().length() > 0);
            assertEquals(1200, response.expiresIn());
        }

        @Test
        @DisplayName("should throw BadCredentialsException when user not found")
        void shouldThrowWhenUserNotFound() {
            when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            LoginRequest request = new LoginRequest("unknown", "password");

            assertThrows(BadCredentialsException.class, () -> authService.login(request));
        }

        @Test
        @DisplayName("should throw BadCredentialsException when password is wrong")
        void shouldThrowWhenPasswordIsWrong() {
            String username = "paciente1";
            Usuario usuario = Usuario.builder()
                    .id(username)
                    .username(username)
                    .password("correctPassword")
                    .rol("PACIENTE")
                    .build();

            when(usuarioRepository.findByUsername(username)).thenReturn(Optional.of(usuario));

            LoginRequest request = new LoginRequest(username, "wrongPassword");

            assertThrows(BadCredentialsException.class, () -> authService.login(request));
        }

        @Test
        @DisplayName("should return token with correct expiration")
        void shouldReturnTokenWithCorrectExpiration() {
            String username = "admin";
            String password = "adminpass";
            String rol = "ADMIN";

            Usuario usuario = Usuario.builder()
                    .id(username)
                    .username(username)
                    .password(password)
                    .rol(rol)
                    .build();

            when(usuarioRepository.findByUsername(username)).thenReturn(Optional.of(usuario));

            LoginRequest request = new LoginRequest(username, password);
            AuthResponse response = authService.login(request);

            assertEquals(20 * 60, response.expiresIn());
        }
    }
}