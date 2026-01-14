package com.clinic.api.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    // O Spring Security precisa que o retorno seja UserDetails para validar o usuário
    UserDetails findByEmail(String email);
}