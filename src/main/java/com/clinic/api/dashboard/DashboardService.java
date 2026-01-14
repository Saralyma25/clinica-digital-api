package com.clinic.api.dashboard;

import com.clinic.api.agendamento.Agendamento;
import com.clinic.api.agendamento.AgendamentoRepository;
import com.clinic.api.dashboard.dto.DashboardResumoDTO;
import com.clinic.api.dashboard.dto.GraficoFaturamentoDTO;
import com.clinic.api.documento.DocumentoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class DashboardService {

    private final AgendamentoRepository repository;
    private final DocumentoRepository documentoRepository;

    public DashboardService(AgendamentoRepository repository, DocumentoRepository documentoRepository) {
        this.repository = repository;
        this.documentoRepository = documentoRepository;
    }

    public DashboardResumoDTO buscarResumoDoDia(UUID medicoId) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.atTime(LocalTime.MAX);

        List<Agendamento> listaHoje = repository.findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(medicoId, inicio, fim);

        long qtdTotal = listaHoje.size();
        long qtdPendentes = listaHoje.stream()
                .filter(a -> "PENDENTE".equals(a.getStatusPagamento()) && Boolean.TRUE.equals(a.getPaciente().getAtendimentoParticular()))
                .count();

        BigDecimal previsao = listaHoje.stream()
                .map(Agendamento::getValorConsulta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal realizado = listaHoje.stream()
                .filter(a -> "PAGO".equals(a.getStatusPagamento()))
                .map(Agendamento::getValorConsulta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardResumoDTO(qtdTotal, qtdPendentes, previsao, realizado);
    }

    public long contarDocumentosNaoLidos() {
        return documentoRepository.countByVistoPeloMedicoFalseAndOrigem("PACIENTE");
    }

    public List<GraficoFaturamentoDTO> gerarDadosGrafico(UUID medicoId) {
        List<GraficoFaturamentoDTO> grafico = new ArrayList<>();
        LocalDate dataBase = LocalDate.now().minusMonths(5);

        for (int i = 0; i < 6; i++) {
            LocalDateTime inicioMes = dataBase.plusMonths(i).withDayOfMonth(1).atStartOfDay();
            LocalDateTime fimMes = dataBase.plusMonths(i).withDayOfMonth(dataBase.plusMonths(i).lengthOfMonth()).atTime(LocalTime.MAX);

            List<Agendamento> agendamentosMes = repository.findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(medicoId, inicioMes, fimMes);

            BigDecimal totalMes = agendamentosMes.stream()
                    .filter(a -> "PAGO".equals(a.getStatusPagamento()))
                    .map(Agendamento::getValorConsulta)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String nomeMes = inicioMes.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")).toUpperCase();
            grafico.add(new GraficoFaturamentoDTO(nomeMes, totalMes));
        }
        return grafico;
    }
}