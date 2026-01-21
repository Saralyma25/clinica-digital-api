package com.clinic.api.dashboard.controller;

import com.clinic.api.dashboard.dto.DashboardResumoDTO;
import com.clinic.api.dashboard.dto.GraficoFaturamentoDTO;
// IMPORTANTE: O Import do Service tem que estar aqui
import com.clinic.api.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService service;

    // O Spring injeta o Service aqui automaticamente se a anotação @Service estiver na classe lá em cima
    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/resumo-dia")
    public ResponseEntity<DashboardResumoDTO> buscarResumoDia(@RequestParam UUID medicoId) {
        return ResponseEntity.ok(service.buscarResumoDoDia(medicoId));
    }

    @GetMapping("/grafico-faturamento")
    public ResponseEntity<List<GraficoFaturamentoDTO>> buscarGrafico(@RequestParam UUID medicoId) {
        return ResponseEntity.ok(service.gerarDadosGrafico(medicoId));
    }
}