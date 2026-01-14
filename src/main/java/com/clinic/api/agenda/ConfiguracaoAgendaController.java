package com.clinic.api.agenda;

import com.clinic.api.medico.Medico;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agenda/configuracoes")
public class ConfiguracaoAgendaController {

    private final AgendaService service;

    public ConfiguracaoAgendaController(AgendaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ConfiguracaoAgenda> salvar(@RequestBody ConfiguracaoAgenda config) {
        // Vinculamos o médico apenas com o ID enviado no JSON
        ConfiguracaoAgenda salva = service.salvarConfiguracao(config);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }
}