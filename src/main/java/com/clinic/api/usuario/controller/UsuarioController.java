package com.clinic.api.usuario.controller;

import com.clinic.api.usuario.dto.UsuarioAtualizacaoDto;
import com.clinic.api.usuario.dto.UsuarioCadastroDto;
import com.clinic.api.usuario.dto.UsuarioDetalhamentoDto;
import com.clinic.api.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // 1. CADASTRAR (POST)
    @PostMapping
    public ResponseEntity<UsuarioDetalhamentoDto> cadastrar(
            @RequestBody @Valid UsuarioCadastroDto dados,
            UriComponentsBuilder uriBuilder
    ) {
        var dto = service.cadastrar(dados);
        var uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    // 2. LISTAR TODOS OU BUSCAR POR TERMO (GET)
    // Exemplo: GET /usuarios (traz tudo)
    // Exemplo: GET /usuarios?termo=gmail (filtra)
    @GetMapping
    public ResponseEntity<List<UsuarioDetalhamentoDto>> listar(
            @RequestParam(required = false) String termo
    ) {
        if (termo != null && !termo.isBlank()) {
            return ResponseEntity.ok(service.buscarPorTermo(termo));
        }
        return ResponseEntity.ok(service.listarTodos());
    }

    // 3. BUSCAR POR ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDetalhamentoDto> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    // 4. ATUALIZAR (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDetalhamentoDto> atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid UsuarioAtualizacaoDto dados
    ) {
        var dto = service.atualizar(id, dados);
        return ResponseEntity.ok(dto);
    }

    // 5. EXCLUIR (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}