package com.clinic.api.agenda.dto;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record AgendaConfigRequest(
        @NotNull UUID medicoId,
        @NotNull DayOfWeek diaSemana,
        @NotNull LocalTime horarioInicio,
        @NotNull LocalTime horarioFim
) {
    // Validação extra no construtor compacto do record
    public AgendaConfigRequest {
        if (horarioInicio.isAfter(horarioFim)) {
            throw new IllegalArgumentException("Horário de início deve ser anterior ao fim.");
        }
    }
}