package com.clinic.api.dashboard;

import com.clinic.api.agendamento.Agendamento;
import com.clinic.api.agendamento.AgendamentoRepository;
import com.clinic.api.dashboard.dto.DashboardResumoDTO;
import com.clinic.api.dashboard.dto.GraficoFaturamentoDTO;
import com.clinic.api.documento.DocumentoRepository;
import com.clinic.api.paciente.Paciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @InjectMocks
    private DashboardService service;

    @Mock
    private AgendamentoRepository repository;

    @Mock
    private DocumentoRepository documentoRepository;

    private UUID medicoId;
    private Paciente pacienteParticular;

    @BeforeEach
    void setup() {
        medicoId = UUID.randomUUID();
        pacienteParticular = new Paciente();
        pacienteParticular.setAtendimentoParticular(true);
    }

    @Test
    @DisplayName("✅ 1. Deve calcular resumo com lista vazia (Zero faturamento)")
    void resumoVazio() {
        when(repository.findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        DashboardResumoDTO dto = service.buscarResumoDoDia(medicoId);

        assertEquals(0, dto.getQtdAtendimentosHoje());
        assertEquals(BigDecimal.ZERO, dto.getFaturamentoPrevisao());
    }

    @Test
    @DisplayName("✅ 2. Deve contar atendimentos corretamente")
    void contarAtendimentos() {
        Agendamento a1 = new Agendamento();
        a1.setValorConsulta(BigDecimal.TEN);
        when(repository.findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(any(), any(), any()))
                .thenReturn(List.of(a1, a1));

        DashboardResumoDTO dto = service.buscarResumoDoDia(medicoId);
        assertEquals(2, dto.getQtdAtendimentosHoje());
    }

    @Test
    @DisplayName("✅ 3. Deve filtrar pacientes pendentes de pagamento")
    void contarPendentes() {
        Agendamento a1 = new Agendamento();
        a1.setPaciente(pacienteParticular);
        a1.setStatusPagamento("PENDENTE");
        a1.setValorConsulta(BigDecimal.valueOf(100));

        when(repository.findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(any(), any(), any()))
                .thenReturn(List.of(a1));

        DashboardResumoDTO dto = service.buscarResumoDoDia(medicoId);
        assertEquals(1, dto.getQtdPendentesPagamento());
    }

    @Test
    @DisplayName("✅ 4. Deve ignorar pendência de convênio (Apenas particular conta como pendente)")
    void ignorarPendenteConvenio() {
        Paciente pConvenio = new Paciente();
        pConvenio.setAtendimentoParticular(false);
        Agendamento a1 = new Agendamento();
        a1.setPaciente(pConvenio);
        a1.setStatusPagamento("PENDENTE");
        a1.setValorConsulta(BigDecimal.ZERO);

        when(repository.findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(any(), any(), any()))
                .thenReturn(List.of(a1));

        DashboardResumoDTO dto = service.buscarResumoDoDia(medicoId);
        assertEquals(0, dto.getQtdPendentesPagamento());
    }

    @Test
    @DisplayName("✅ 5. Deve somar faturamento previsto total (Pendente + Pago)")
    void faturamentoPrevisto() {
        Agendamento a1 = new Agendamento();
        a1.setValorConsulta(new BigDecimal("150.00"));
        Agendamento a2 = new Agendamento();
        a2.setValorConsulta(new BigDecimal("200.00"));

        when(repository.findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(any(), any(), any()))
                .thenReturn(List.of(a1, a2));

        DashboardResumoDTO dto = service.buscarResumoDoDia(medicoId);
        assertEquals(new BigDecimal("350.00"), dto.getFaturamentoPrevisao());
    }

    @Test
    @DisplayName("✅ 6. Deve somar faturamento realizado apenas para status PAGO")
    void faturamentoRealizado() {
        Agendamento a1 = new Agendamento();
        a1.setStatusPagamento("PAGO");
        a1.setValorConsulta(new BigDecimal("100.00"));
        a1.setPaciente(pacienteParticular); // <--- Adicionei esta linha crucial

        when(repository.findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(any(), any(), any()))
                .thenReturn(List.of(a1));

        DashboardResumoDTO dto = service.buscarResumoDoDia(medicoId);
        assertEquals(new BigDecimal("100.00"), dto.getFaturamentoRealizado());
    }

    @Test
    @DisplayName("✅ 7. Deve contar documentos não lidos do paciente")
    void documentosNaoLidos() {
        when(documentoRepository.countByVistoPeloMedicoFalseAndOrigem("PACIENTE")).thenReturn(5L);
        long total = service.contarDocumentosNaoLidos();
        assertEquals(5, total);
    }

    @Test
    @DisplayName("✅ 8. Deve gerar lista de gráfico com 6 meses")
    void mesesGrafico() {
        List<GraficoFaturamentoDTO> lista = service.gerarDadosGrafico(medicoId);
        assertEquals(6, lista.size());
    }

    @Test
    @DisplayName("✅ 9. Deve somar faturamento mensal no gráfico ignorando não pagos")
    void somaMensalGrafico() {
        Agendamento a1 = new Agendamento();
        a1.setStatusPagamento("PAGO");
        a1.setValorConsulta(new BigDecimal("1000.00"));

        when(repository.findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(any(), any(), any()))
                .thenReturn(List.of(a1));

        List<GraficoFaturamentoDTO> lista = service.gerarDadosGrafico(medicoId);
        // O último mês da lista é o atual
        assertEquals(new BigDecimal("1000.00"), lista.get(5).getValorTotal());
    }

    @Test
    @DisplayName("✅ 10. Deve formatar nome do mês corretamente no gráfico")
    void formatoMes() {
        List<GraficoFaturamentoDTO> lista = service.gerarDadosGrafico(medicoId);
        String mesAtual = LocalDate.now().getMonth().getDisplayName(java.time.format.TextStyle.SHORT, new java.util.Locale("pt", "BR")).toUpperCase();
        assertEquals(mesAtual, lista.get(5).getMes());
    }
}