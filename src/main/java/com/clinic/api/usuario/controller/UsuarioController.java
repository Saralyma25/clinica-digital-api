package com.clinic.api.usuario.controller;

import com.clinic.api.usuario.Usuario;
import com.clinic.api.usuario.domain.UsuarioRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository repository;

    public UsuarioController(UsuarioRepository repository) {
        this.repository = repository;
    }

    // 1. CADASTRAR (POST)
    @PostMapping
    @Transactional
    public ResponseEntity<Usuario> cadastrar(@RequestBody @Valid Usuario usuario) {
        // Garantimos que o usuário comece ativo
        usuario.setAtivo(true);
        Usuario salvo = repository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // 2. LISTAR TODOS (GET)
    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        // Retorna apenas os ativos ou todos para teste inicial
        List<Usuario> usuarios = repository.findAll();
        return ResponseEntity.ok(usuarios);
    }

    // 3. BUSCAR POR ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable UUID id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. ALTERAR (PUT)
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Usuario> alterar(@PathVariable UUID id, @RequestBody Usuario dadosNovos) {
        return repository.findById(id)
                .map(usuarioExistente -> {
                    usuarioExistente.setEmail(dadosNovos.getEmail());
                    usuarioExistente.setRole(dadosNovos.getRole());
                    // Nota: Senha geralmente tem um endpoint próprio, mas para teste:
                    if (dadosNovos.getSenha() != null) {
                        usuarioExistente.setSenha(dadosNovos.getSenha());
                    }
                    repository.save(usuarioExistente);
                    return ResponseEntity.ok(usuarioExistente);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 5. EXCLUIR LÓGICO (DELETE)
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        return repository.findById(id)
                .map(usuario -> {
                    usuario.setAtivo(false); // Recomendação Mateus: Inativar em vez de apagar
                    repository.save(usuario);
                    return ResponseEntity.noContent().<Void>build(); // Status 204
                })
                .orElse(ResponseEntity.notFound().build());
    }
}