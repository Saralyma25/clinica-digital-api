package com.clinic.api.prontuario.controller;

import com.clinic.api.prontuario.Prontuario; // Apenas se necessário para algum método específico
import com.clinic.api.prontuario.dto.*;
import com.clinic.api.prontuario.service.ProntuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/prontuarios")
public class ProntuarioController {

    private final ProntuarioService service;

    public ProntuarioController(ProntuarioService service) {
        this.service = service;
    }

    // --- 1. A CAPA DA PASTA (Folha de Rosto) ---
    @GetMapping("/paciente/{pacienteId}/folha-rosto")
    public ResponseEntity<FolhaDeRostoDTO> obterFolhaDeRosto(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(service.obterFolhaDeRosto(pacienteId));
    }

    // --- 2. HISTÓRICO COMPLETO (Timeline) ---
    // CORREÇÃO: Agora retorna List<ProntuarioResponse> para bater com o Service
    @GetMapping("/paciente/{pacienteId}/historico")
    public ResponseEntity<List<ProntuarioResponse>> obterHistorico(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(service.listarHistoricoPaciente(pacienteId));
    }

    // --- 3. SALVAR EVOLUÇÃO (Escrever na folha) ---
    // CORREÇÃO: Recebe 'ProntuarioRequest' (DTO) e não a Entidade
    @PostMapping
    public ResponseEntity<ProntuarioResponse> salvar(
            @RequestBody @Valid ProntuarioRequest request,
            @RequestHeader("id-medico-logado") UUID idMedicoLogado) {

        var response = service.salvar(request, idMedicoLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- 4. SALVAR DADOS FIXOS (Alergias/Doenças) ---
    @PostMapping("/dados-fixos")
    public ResponseEntity<Void> salvarDadosFixos(@RequestBody @Valid DadosClinicosRequest request) {
        service.salvarDadosFixos(request);
        return ResponseEntity.ok().build();
    }

    // --- 5. BUSCAR PRONTUÁRIO ESPECÍFICO ---
    @GetMapping("/agendamento/{agendamentoId}")
    public ResponseEntity<ProntuarioResponse> buscarPorAgendamento(@PathVariable UUID agendamentoId) {
        return ResponseEntity.ok(service.buscarPorAgendamento(agendamentoId));
    }
}