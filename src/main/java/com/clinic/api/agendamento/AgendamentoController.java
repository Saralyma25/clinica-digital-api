package com.clinic.api.agendamento;

import com.clinic.api.agendamento.dto.AgendamentoRequest;
import com.clinic.api.agendamento.dto.AgendamentoResponse;
import com.clinic.api.medico.Medico;
import com.clinic.api.paciente.Paciente;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendamentoService service;

    // Injeção de dependência via construtor manual
    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    // --- 1. AGENDAR (POST) ---
    @PostMapping
    public ResponseEntity<AgendamentoResponse> agendar(@RequestBody @Valid AgendamentoRequest request) {

        // Conversão Manual DTO -> Entidade
        Agendamento agendamentoParaSalvar = new Agendamento();
        agendamentoParaSalvar.setDataConsulta(request.getDataConsulta());

        // Criamos objetos "Shell" (Casca) apenas com o ID para o Service buscar no banco
        Medico medico = new Medico();
        medico.setId(request.getMedicoId());
        agendamentoParaSalvar.setMedico(medico);

        Paciente paciente = new Paciente();
        paciente.setId(request.getPacienteId());
        agendamentoParaSalvar.setPaciente(paciente);

        // Chama o Service (que contém as regras de negócio, validação de horário, etc.)
        Agendamento agendamentoSalvo = service.agendar(agendamentoParaSalvar);

        // Retorna 201 Created com o DTO de resposta
        return ResponseEntity.status(HttpStatus.CREATED).body(new AgendamentoResponse(agendamentoSalvo));
    }

    // --- 2. LISTAR TODOS (GET) ---
    @GetMapping
    public ResponseEntity<List<AgendamentoResponse>> listarTodos() {
        List<AgendamentoResponse> lista = service.listarTodos().stream()
                .map(AgendamentoResponse::new) // Referência ao construtor do DTO
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
    // Usamos PATCH pois estamos mudando apenas o status, não o objeto todo
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<Void> confirmar(@PathVariable UUID id) {
        service.confirmarAgendamento(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // --- 5. CANCELAR AGENDAMENTO (DELETE) ---
    // Mapeamos para DELETE, mas internamente o Service faz apenas a mudança de status (Soft Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id) {
        service.cancelar(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}