package com.clinic.api.agenda.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record BloqueioRequest(
        @NotNull UUID medicoId,
        @NotNull LocalDateTime inicio,
        @NotNull LocalDateTime fim,
        String motivo
) {}