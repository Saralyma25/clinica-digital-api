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
public class AgendaController {

    private final AgendaService service;

    public AgendaController(AgendaService service) {
        this.service = service;
    }

    // 1. Configurar Horário (Recurso: Configuração)
    // URL: POST /agenda/configuracao
    @PostMapping("/configuracao")
    public ResponseEntity<ConfiguracaoAgenda> salvarConfiguracao(@RequestBody @Valid AgendaConfigRequest request) {
        return ResponseEntity.ok(service.salvarConfiguracao(request));
    }

    // 2. Criar Bloqueio (Recurso: Bloqueios)
    // URL: POST /agenda/bloqueios
    @PostMapping("/bloqueios")
    public ResponseEntity<BloqueioAgenda> criarBloqueio(@RequestBody @Valid BloqueioRequest request) {
        return ResponseEntity.ok(service.criarBloqueio(request));
    }

    // 3. Buscar Disponibilidade
    // URL: GET /agenda/disponibilidade?medicoId=...&data=...
    @GetMapping("/disponibilidade")
    public ResponseEntity<List<LocalDateTime>> buscarDisponibilidade(
            @RequestParam UUID medicoId,
            @RequestParam LocalDate data) {
        return ResponseEntity.ok(service.listarHorariosDisponiveis(medicoId, data));
    }
}