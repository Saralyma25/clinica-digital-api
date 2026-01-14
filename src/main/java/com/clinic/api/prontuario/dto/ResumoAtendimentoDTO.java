package com.clinic.api.prontuario.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ResumoAtendimentoDTO {
    private LocalDateTime data;
    private String especialidade;
    private String medico;

    public ResumoAtendimentoDTO(LocalDateTime data, String especialidade, String medico) {
        this.data = data;
        this.especialidade = especialidade;
        this.medico = medico;
    }

    // Getters Manuais (Sem Lombok)
    public LocalDateTime getData() { return data; }
    public String getEspecialidade() { return especialidade; }
    public String getMedico() { return medico; }
}