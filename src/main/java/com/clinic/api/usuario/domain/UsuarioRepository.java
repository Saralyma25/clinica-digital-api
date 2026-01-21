package com.clinic.api.usuario.domain;

import com.clinic.api.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    // Retornamos a classe Usuario diretamente para facilitar o uso no Controller/Service
    Usuario findByEmail(String email);
}