package com.clinic.api.usuario.domain;

import com.clinic.api.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    // Busca flexível: encontra "mateus" em "mateus@gmail.com"
    // Usamos isso para o requisito "Buscar por Nome/Email"
    List<Usuario> findByEmailContainingIgnoreCase(String email);
}