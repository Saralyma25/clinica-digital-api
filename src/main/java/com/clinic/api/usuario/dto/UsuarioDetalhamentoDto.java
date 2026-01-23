package com.clinic.api.usuario.dto;

import com.clinic.api.usuario.Usuario;
import com.clinic.api.usuario.domain.UserRole;
import java.util.UUID;

public record UsuarioDetalhamentoDto(
        UUID id,
        String email,
        UserRole role,
        Boolean ativo
) {
    // Construtor auxiliar para converter Entidade -> DTO
    public UsuarioDetalhamentoDto(Usuario usuario) {
        this(usuario.getId(), usuario.getEmail(), usuario.getRole(), usuario.getAtivo());
    }
}