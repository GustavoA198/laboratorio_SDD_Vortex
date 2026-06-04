package com.clinica.service;

import com.clinica.dto.AuthResponse;
import com.clinica.dto.LoginRequest;
import com.clinica.model.Usuario;
import com.clinica.repository.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

/**
 * Service for authentication operations.
 */
@Service
public class AuthService {

    private static final long EXPIRATION_SECONDS = 20 * 60; // 20 minutes

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    /**
     * Authenticates a user with username and password.
     *
     * @param request the login request with credentials
     * @return the authentication response with JWT token
     * @throws BadCredentialsException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!usuario.getPassword().equals(request.password())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String token = jwtService.createToken(usuario.getUsername(), usuario.getRol());

        return new AuthResponse(token, EXPIRATION_SECONDS);
    }
}