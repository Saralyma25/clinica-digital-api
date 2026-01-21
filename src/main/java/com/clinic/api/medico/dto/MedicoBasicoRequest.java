package com.clinic.api.medico.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;


public record MedicoBasicoRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotNull UUID clinicaId
) {}