package com.clinic.api.usuario.service;

import com.clinic.api.usuario.Usuario;
import com.clinic.api.usuario.domain.UsuarioRepository;
import com.clinic.api.usuario.dto.UsuarioAtualizacaoDto;
import com.clinic.api.usuario.dto.UsuarioCadastroDto;
import com.clinic.api.usuario.dto.UsuarioDetalhamentoDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UsuarioDetalhamentoDto cadastrar(UsuarioCadastroDto dados) {
        if (repository.existsByEmail(dados.email())) {
            throw new IllegalArgumentException("Email já cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(dados.email());
        usuario.setSenha(dados.senha()); // TODO: Lembrar de aplicar BCryptEncoder aqui futuramente
        usuario.setRole(dados.role());
        usuario.setAtivo(true);

        repository.save(usuario);

        return toDetalhamentoDto(usuario);
    }

    public UsuarioDetalhamentoDto buscarPorId(UUID id) {
        Usuario usuario = buscarUsuarioOuLancarErro(id);
        return toDetalhamentoDto(usuario);
    }

    public List<UsuarioDetalhamentoDto> listarTodos() {
        return repository.findAll().stream()
                .map(this::toDetalhamentoDto)
                .collect(Collectors.toList());
    }

    public List<UsuarioDetalhamentoDto> buscarPorTermo(String termo) {
        return repository.findByEmailContainingIgnoreCase(termo).stream()
                .map(this::toDetalhamentoDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioDetalhamentoDto atualizar(UUID id, UsuarioAtualizacaoDto dados) {
        Usuario usuario = buscarUsuarioOuLancarErro(id);

        // 1. Atualizar Email (com validação de duplicidade)
        if (dados.email() != null && !dados.email().equals(usuario.getEmail())) {
            if (repository.existsByEmail(dados.email())) {
                throw new IllegalArgumentException("Email já cadastrado.");
            }
            usuario.setEmail(dados.email());
        }

        // 2. Atualizar Role (Perfil de acesso)
        if (dados.role() != null) {
            usuario.setRole(dados.role());
        }

        // 3. Atualizar Status (Ativar/Inativar via PUT)
        // Corrigido: O DTO tem 'ativo', mas não tem 'senha'.
        if (dados.ativo() != null) {
            usuario.setAtivo(dados.ativo());
        }

        // Necessário chamar save() explicitamente para passar no teste com Mockito
        repository.save(usuario);

        return toDetalhamentoDto(usuario);
    }

    @Transactional
    public void excluir(UUID id) {
        Usuario usuario = buscarUsuarioOuLancarErro(id);

        // Exclusão Lógica
        usuario.setAtivo(false);

        // Necessário chamar save() explicitamente para passar no teste com Mockito
        repository.save(usuario);
    }

    // --- Métodos Privados ---

    private Usuario buscarUsuarioOuLancarErro(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }

    private UsuarioDetalhamentoDto toDetalhamentoDto(Usuario usuario) {
        return new UsuarioDetalhamentoDto(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.getAtivo()
        );
    }
}