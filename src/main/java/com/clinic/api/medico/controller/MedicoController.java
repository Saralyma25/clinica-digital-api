package com.clinic.api.medico.controller;

import com.clinic.api.medico.dto.MedicoBasicoRequest; // DTO do Cadastro Rápido
import com.clinic.api.medico.dto.MedicoRequest;
import com.clinic.api.medico.dto.MedicoResponse;
import com.clinic.api.medico.service.MedicoService;
// CORREÇÃO: O import correto do Enum
import com.clinic.api.medico.enun.Especialidade;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/medicos")
public class MedicoController {

    private final MedicoService service;

    public MedicoController(MedicoService service) {
        this.service = service;
    }

    // --- CADASTRO RÁPIDO (Google) ---
    @PostMapping("/rapido")
    public ResponseEntity<MedicoResponse> cadastrarRapido(@RequestBody @Valid MedicoBasicoRequest request) {
        var response = service.cadastrarRapido(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- CADASTRO COMPLETO / TRADICIONAL ---
    @PostMapping
    public ResponseEntity<MedicoResponse> cadastrarCompleto(@RequestBody @Valid MedicoRequest request) {
        var response = service.cadastrarCompleto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MedicoResponse>> listar() {
        return ResponseEntity.ok(service.listarTodosAtivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/crm/{crm}")
    public ResponseEntity<MedicoResponse> buscarPorCrm(@PathVariable String crm) {
        return ResponseEntity.ok(service.buscarPorCrm(crm));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<MedicoResponse>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(service.buscarPorNome(nome));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicoResponse> atualizar(@PathVariable UUID id, @RequestBody MedicoRequest request) {
        var response = service.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}