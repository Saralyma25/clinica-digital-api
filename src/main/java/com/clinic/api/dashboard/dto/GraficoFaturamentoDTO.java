package com.clinic.api.dashboard.dto;

import java.math.BigDecimal;

public class GraficoFaturamentoDTO {
    private String mes; // Ex: "JAN", "FEV"
    private BigDecimal valorTotal;

    public GraficoFaturamentoDTO(String mes, BigDecimal valorTotal) {
        this.mes = mes;
        this.valorTotal = valorTotal;
    }

    public String getMes() { return mes; }
    public BigDecimal getValorTotal() { return valorTotal; }
}