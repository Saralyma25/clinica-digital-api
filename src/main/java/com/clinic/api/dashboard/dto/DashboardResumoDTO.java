package com.clinic.api.dashboard.dto;

import java.math.BigDecimal;

public record DashboardResumoDTO(
        long qtdAtendimentosHoje,
        long qtdPendentesPagamento,
        long qtdExamesNaoLidos,
        BigDecimal faturamentoPrevisao,
        BigDecimal faturamentoRealizado
) {}