package com.clinic.api.prontuario.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ProntuarioRequest {

    @NotNull(message = "O ID do agendamento é obrigatório")
    private UUID agendamentoId;

    private String queixaPrincipal;


    private String diagnostico;
    private String prescricaoMedica;

    // Getters e Setters
    public UUID getAgendamentoId() { return agendamentoId; }
    public void setAgendamentoId(UUID agendamentoId) { this.agendamentoId = agendamentoId; }
    public String getQueixaPrincipal() { return queixaPrincipal; }
    public void setQueixaPrincipal(String queixaPrincipal) { this.queixaPrincipal = queixaPrincipal; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getPrescricaoMedica() { return prescricaoMedica; }
    public void setPrescricaoMedica(String prescricaoMedica) { this.prescricaoMedica = prescricaoMedica; }
}