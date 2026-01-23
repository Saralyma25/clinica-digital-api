package com.clinic.api.usuario.dto;

import com.clinic.api.usuario.domain.UserRole;
import jakarta.validation.constraints.Email;

// Note que aqui a senha não está presente. Senha se troca em endpoint específico por segurança.
public record UsuarioAtualizacaoDto(
        @Email(message = "Formato de email inválido")
        String email,

        UserRole role,

        Boolean ativo
) {}