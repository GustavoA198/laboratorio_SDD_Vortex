package com.clinica.repository;

import com.clinica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository for Usuario entity.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    /**
     * Finds a Usuario by username.
     *
     * @param username the username to search
     * @return Optional containing the Usuario if found
     */
    Optional<Usuario> findByUsername(String username);
}