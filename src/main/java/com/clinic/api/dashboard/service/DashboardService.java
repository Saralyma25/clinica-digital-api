package com.clinic.api.dashboard.service;

import com.clinic.api.agendamento.Agendamento;
import com.clinic.api.agendamento.domain.AgendamentoRepository;
import com.clinic.api.dashboard.dto.DashboardResumoDTO;
import com.clinic.api.dashboard.dto.GraficoFaturamentoDTO;
import com.clinic.api.documento.domain.DocumentoRepository;
import org.springframework.stereotype.Service; // <--- OBRIGATÓRIO

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service // <--- ISSO RESOLVE O "COULD NOT AUTOWIRE"
public class DashboardService {

    private final AgendamentoRepository agendamentoRepository;
    private final DocumentoRepository documentoRepository;

    public DashboardService(AgendamentoRepository agendamentoRepository,
                            DocumentoRepository documentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.documentoRepository = documentoRepository;
    }

    public DashboardResumoDTO buscarResumoDoDia(UUID medicoId) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.atTime(LocalTime.MAX);

        List<Agendamento> listaHoje = agendamentoRepository.findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(medicoId, inicio, fim);

        long qtdTotal = listaHoje.size();

        long qtdPendentes = listaHoje.stream()
                .filter(a -> "PENDENTE".equals(a.getStatusPagamento()) &&
                        Boolean.TRUE.equals(a.getPaciente().getAtendimentoParticular()))
                .count();

        long examesPendentes = documentoRepository.countByVistoPeloMedicoFalseAndOrigem("PACIENTE");

        BigDecimal previsao = listaHoje.stream()
                .map(Agendamento::getValorConsulta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal realizado = listaHoje.stream()
                .filter(a -> "PAGO".equals(a.getStatusPagamento()) || "CONVENIO_APROVADO".equals(a.getStatusPagamento()))
                .map(Agendamento::getValorConsulta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Retorna o Record com 5 argumentos
        return new DashboardResumoDTO(qtdTotal, qtdPendentes, examesPendentes, previsao, realizado);
    }

    public List<GraficoFaturamentoDTO> gerarDadosGrafico(UUID medicoId) {
        List<GraficoFaturamentoDTO> grafico = new ArrayList<>();
        LocalDate dataBase = LocalDate.now().minusMonths(5);

        for (int i = 0; i < 6; i++) {
            LocalDate mesReferencia = dataBase.plusMonths(i);
            LocalDateTime inicioMes = mesReferencia.withDayOfMonth(1).atStartOfDay();
            LocalDateTime fimMes = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth()).atTime(LocalTime.MAX);

            List<Agendamento> agendamentosMes = agendamentoRepository.findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(medicoId, inicioMes, fimMes);

            BigDecimal totalMes = agendamentosMes.stream()
                    .filter(a -> "PAGO".equals(a.getStatusPagamento()) || "CONVENIO_APROVADO".equals(a.getStatusPagamento()))
                    .map(Agendamento::getValorConsulta)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String nomeMes = mesReferencia.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")).toUpperCase();

            grafico.add(new GraficoFaturamentoDTO(nomeMes, totalMes));
        }
        return grafico;
    }
}