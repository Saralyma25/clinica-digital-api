package com.clinic.api.agendamento.controller;

import com.clinic.api.agenda.service.AgendaService;
import com.clinic.api.agendamento.Agendamento;
import com.clinic.api.agendamento.dto.AgendamentoRequest;
import com.clinic.api.agendamento.dto.AgendamentoResponse;
import com.clinic.api.agendamento.service.AgendamentoService;
import com.clinic.api.medico.Medico;
import com.clinic.api.paciente.Paciente;
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

    // --- NOVO ENDPOINT: Buscar horários disponíveis (Motor de busca do Dia 06) ---
    @GetMapping("/disponibilidade")
    public ResponseEntity<List<LocalDateTime>> buscarDisponibilidade(
            @RequestParam UUID medicoId,
            @RequestParam LocalDate data) {

        List<LocalDateTime> disponiveis = agendaService.listarHorariosDisponiveis(medicoId, data);
        return ResponseEntity.ok(disponiveis);
    }

    // --- 1. AGENDAR (POST) ---
    @PostMapping
    public ResponseEntity<AgendamentoResponse> agendar(@RequestBody @Valid AgendamentoRequest request) {
        Agendamento agendamentoParaSalvar = new Agendamento();

        // Dados básicos da consulta
        agendamentoParaSalvar.setDataConsulta(request.getDataConsulta());

        // NOVOS CAMPOS: Capturando dados financeiros e de convênio do DTO
        agendamentoParaSalvar.setFormaPagamento(request.getFormaPagamento());
        agendamentoParaSalvar.setNomeConvenio(request.getNomeConvenio());
        agendamentoParaSalvar.setNumeroCarteirinha(request.getNumeroCarteirinha());

        // Associação com Médico (Shell Object)
        Medico medico = new Medico();
        medico.setId(request.getMedicoId());
        agendamentoParaSalvar.setMedico(medico);

        // Associação com Paciente (Shell Object)
        Paciente paciente = new Paciente();
        paciente.setId(request.getPacienteId());
        agendamentoParaSalvar.setPaciente(paciente);

        // O Service aplicará as regras de bifurcação (Convênio vs Particular)
        Agendamento agendamentoSalvo = service.agendar(agendamentoParaSalvar);

        return ResponseEntity.status(HttpStatus.CREATED).body(new AgendamentoResponse(agendamentoSalvo));
    }

    // --- 2. LISTAR TODOS (GET) ---
    @GetMapping
    public ResponseEntity<List<AgendamentoResponse>> listarTodos() {
        List<AgendamentoResponse> lista = service.listarTodos().stream()
                .map(AgendamentoResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    // --- 3. BUSCAR POR ID (GET) ---
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponse> buscarPorId(@PathVariable UUID id) {
        Agendamento agendamento = service.buscarPorId(id);
        return ResponseEntity.ok(new AgendamentoResponse(agendamento));
    }

    // --- 4. CONFIRMAR AGENDAMENTO (PATCH) ---
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<Void> confirmar(@PathVariable UUID id) {
        service.confirmarAgendamento(id);
        return ResponseEntity.noContent().build();
    }

    // --- 5. CANCELAR AGENDAMENTO (DELETE) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id) {
        service.cancelar(id);
        return ResponseEntity.noContent().build();
    }
    // --- NOVO ENDPOINT (Dia 06): Lista de Atendimentos do Dia (Visão da Secretária) ---
    // Exemplo de chamada: GET /agendamentos/diario?medicoId=...&data=2023-10-25
    @GetMapping("/diario")
    public ResponseEntity<List<com.clinic.api.agendamento.dto.AtendimentoDiarioDTO>> listarDiario(
            @RequestParam UUID medicoId,
            @RequestParam LocalDate data) {

        var lista = service.listarAtendimentosDoDia(medicoId, data);
        return ResponseEntity.ok(lista);
    }
}