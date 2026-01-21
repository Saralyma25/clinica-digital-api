package com.clinic.api.prontuario.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record DadosClinicosRequest(
        @NotNull UUID pacienteId,
        String comorbidades,
        String alergias,
        String observacoesPermanentes
) {}