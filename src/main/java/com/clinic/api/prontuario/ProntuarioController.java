package com.clinic.api.prontuario;

import com.clinic.api.prontuario.dto.FolhaDeRostoDTO;
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
    // O médico abre o paciente e chama isso aqui primeiro.
    @GetMapping("/paciente/{pacienteId}/folha-rosto")
    public ResponseEntity<FolhaDeRostoDTO> obterFolhaDeRosto(@PathVariable UUID pacienteId) {
        FolhaDeRostoDTO folha = service.obterFolhaDeRosto(pacienteId);
        return ResponseEntity.ok(folha);
    }

    // --- 2. HISTÓRICO COMPLETO (Timeline) ---
    // Se o médico quiser ler os detalhes antigos
    @GetMapping("/paciente/{pacienteId}/historico")
    public ResponseEntity<List<Prontuario>> obterHistorico(@PathVariable UUID pacienteId) {
        List<Prontuario> historico = service.listarHistoricoPaciente(pacienteId);
        return ResponseEntity.ok(historico);
    }

    // --- 3. SALVAR EVOLUÇÃO (Escrever na folha) ---
    // IMPORTANTE: Aqui simulamos que o ID do médico vem no Header ou Token (mockado por enquanto)
    @PostMapping
    public ResponseEntity<Prontuario> salvar(
            @RequestBody Prontuario prontuario,
            @RequestHeader("id-medico-logado") UUID idMedicoLogado) {

        Prontuario salvo = service.salvar(prontuario, idMedicoLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // --- 4. BUSCAR PRONTUÁRIO ESPECÍFICO DE UM AGENDAMENTO ---
    @GetMapping("/agendamento/{agendamentoId}")
    public ResponseEntity<Prontuario> buscarPorAgendamento(@PathVariable UUID agendamentoId) {
        Prontuario prontuario = service.buscarPorAgendamento(agendamentoId);
        return ResponseEntity.ok(prontuario);
    }
}