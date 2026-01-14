package com.clinic.api.dashboard.dto;

import java.math.BigDecimal;

public class DashboardResumoDTO {
    private long qtdAtendimentosHoje;
    private long qtdPendentesPagamento;
    private BigDecimal faturamentoPrevisao; // O que está agendado
    private BigDecimal faturamentoRealizado; // O que já foi pago

    public DashboardResumoDTO(long qtdAtendimentosHoje, long qtdPendentesPagamento, BigDecimal faturamentoPrevisao, BigDecimal faturamentoRealizado) {
        this.qtdAtendimentosHoje = qtdAtendimentosHoje;
        this.qtdPendentesPagamento = qtdPendentesPagamento;
        this.faturamentoPrevisao = faturamentoPrevisao;
        this.faturamentoRealizado = faturamentoRealizado;
    }

    // Getters
    public long getQtdAtendimentosHoje() { return qtdAtendimentosHoje; }
    public long getQtdPendentesPagamento() { return qtdPendentesPagamento; }
    public BigDecimal getFaturamentoPrevisao() { return faturamentoPrevisao; }
    public BigDecimal getFaturamentoRealizado() { return faturamentoRealizado; }
}