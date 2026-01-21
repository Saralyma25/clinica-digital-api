package com.clinic.api.paciente.controller;

import com.clinic.api.paciente.dto.PacienteBasicoRequest;
import com.clinic.api.paciente.dto.PacienteRequest;
import com.clinic.api.paciente.dto.PacienteResponse;
import com.clinic.api.paciente.dto.TimelineDTO;
import com.clinic.api.paciente.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    // Endpoint para Login Social / Cadastro Rápido
    @PostMapping("/rapido")
    public ResponseEntity<PacienteResponse> cadastrarRapido(@RequestBody @Valid PacienteBasicoRequest request) {
        var response = service.cadastrarRapido(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Endpoint para Cadastro Tradicional Completo
    @PostMapping
    public ResponseEntity<PacienteResponse> cadastrarCompleto(@RequestBody @Valid PacienteRequest request) {
        var response = service.cadastrarCompleto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponse>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    // Endpoint da Timeline (Histórico Médico)
    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<TimelineDTO>> buscarTimeline(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarTimelineCompleta(id));
    }
}