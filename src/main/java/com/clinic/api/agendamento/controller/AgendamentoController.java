package com.clinic.api.agendamento.controller;

import com.clinic.api.agenda.service.AgendaService;
import com.clinic.api.agendamento.Agendamento;
import com.clinic.api.agendamento.dto.AgendamentoRequest;
import com.clinic.api.agendamento.dto.AgendamentoResponse;
import com.clinic.api.agendamento.dto.AtendimentoDiarioDTO;
import com.clinic.api.agendamento.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendamentoService service;
    private final AgendaService agendaService;

    public AgendamentoController(AgendamentoService service, AgendaService agendaService) {
        this.service = service;
        this.agendaService = agendaService;
    }

    // --- Motor de Disponibilidade ---
    @GetMapping("/disponibilidade")
    public ResponseEntity<List<LocalDateTime>> buscarDisponibilidade(
            @RequestParam UUID medicoId,
            @RequestParam LocalDate data) {
        return ResponseEntity.ok(agendaService.listarHorariosDisponiveis(medicoId, data));
    }

    // --- AGENDAR (Refatorado: Passa o DTO para o Service) ---
    @PostMapping
    public ResponseEntity<AgendamentoResponse> agendar(@RequestBody @Valid AgendamentoRequest request) {
        var response = service.agendar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- LISTAR TODOS ---
    @GetMapping
    public ResponseEntity<List<AgendamentoResponse>> listarTodos() {
        List<AgendamentoResponse> lista = service.listarTodos().stream()
                .map(AgendamentoResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    // --- BUSCAR POR ID ---
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponse> buscarPorId(@PathVariable UUID id) {
        Agendamento agendamento = service.buscarPorId(id);
        return ResponseEntity.ok(new AgendamentoResponse(agendamento));
    }

    // --- AÇÕES DO FLUXO ---
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<Void> confirmar(@PathVariable UUID id) {
        service.confirmarAgendamento(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id) {
        service.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    // --- DASHBOARD DIÁRIO ---
    @GetMapping("/diario")
    public ResponseEntity<List<AtendimentoDiarioDTO>> listarDiario(
            @RequestParam UUID medicoId,
            @RequestParam LocalDate data) {
        return ResponseEntity.ok(service.listarAtendimentosDoDia(medicoId, data));
    }
}