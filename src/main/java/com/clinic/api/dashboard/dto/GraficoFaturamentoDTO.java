package com.clinic.api.dashboard.dto;

import java.math.BigDecimal;

public record GraficoFaturamentoDTO(
        String mes,
        BigDecimal valorTotal
) {}