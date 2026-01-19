package com.clinic.api.paciente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Esse DTO serve EXCLUSIVAMENTE para a entrada inicial (Google ou cadastro rápido)
public record PacienteBasicoRequest(
    @NotBlank(message = "O nome é obrigatório")
    String nome,

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    String email
) {}