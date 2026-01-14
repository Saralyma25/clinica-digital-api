package com.clinic.api.dashboard;

import com.clinic.api.dashboard.dto.DashboardResumoDTO;
import com.clinic.api.dashboard.dto.GraficoFaturamentoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final com.clinic.api.dashboard.DashboardService service;

    public DashboardController(com.clinic.api.dashboard.DashboardService service) {
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