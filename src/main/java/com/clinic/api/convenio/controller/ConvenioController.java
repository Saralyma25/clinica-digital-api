package com.clinic.api.convenio.controller;

import com.clinic.api.convenio.dto.ConvenioRequest;
import com.clinic.api.convenio.dto.ConvenioResponse;
import com.clinic.api.convenio.service.ConvenioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/convenios")
public class ConvenioController {

    private final ConvenioService service;

    public ConvenioController(ConvenioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ConvenioResponse> cadastrar(@RequestBody @Valid ConvenioRequest request) {
        var response = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ConvenioResponse>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ConvenioResponse>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(service.buscarPorNome(nome));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConvenioResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    // --- NOVO MÉTODO ADICIONADO ---
    @PutMapping("/{id}")
    public ResponseEntity<ConvenioResponse> atualizar(@PathVariable UUID id, @RequestBody @Valid ConvenioRequest request) {
        var response = service.atualizar(id, request);
        return ResponseEntity.ok(response);
    }
    // -----------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}