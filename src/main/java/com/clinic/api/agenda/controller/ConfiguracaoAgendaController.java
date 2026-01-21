package com.clinic.api.agenda.controller;

import com.clinic.api.agenda.domain.BloqueioAgenda;
import com.clinic.api.agenda.domain.ConfiguracaoAgenda;
import com.clinic.api.agenda.dto.AgendaConfigRequest;
import com.clinic.api.agenda.dto.BloqueioRequest;
import com.clinic.api.agenda.service.AgendaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/agenda")
public class ConfiguracaoAgendaController {

    private final AgendaService service;

    public ConfiguracaoAgendaController(AgendaService service) {
        this.service = service;
    }

    // 1. Configurar horário de trabalho (Ex: Segundas das 08h às 18h)
    @PostMapping("/configurar")
    public ResponseEntity<ConfiguracaoAgenda> salvarConfiguracao(@RequestBody @Valid AgendaConfigRequest request) {
        return ResponseEntity.ok(service.salvarConfiguracao(request));
    }

    // 2. Criar Bloqueio (Ex: Almoço hoje das 12h às 13h)
    @PostMapping("/bloquear")
    public ResponseEntity<BloqueioAgenda> criarBloqueio(@RequestBody @Valid BloqueioRequest request) {
        return ResponseEntity.ok(service.criarBloqueio(request));
    }

    // 3. Buscar Disponibilidade (Para o Paciente ver as "bolinhas verdes")
    @GetMapping("/disponibilidade")
    public ResponseEntity<List<LocalDateTime>> buscarDisponibilidade(
            @RequestParam UUID medicoId,
            @RequestParam LocalDate data) {
        return ResponseEntity.ok(service.listarHorariosDisponiveis(medicoId, data));
    }
}