package com.clinic.api.agendamento.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public class AgendamentoRequest {

    @NotNull(message = "O ID do médico é obrigatório")
    private UUID medicoId;

    @NotNull(message = "O ID do paciente é obrigatório")
    private UUID pacienteId;

    @NotNull(message = "A data da consulta é obrigatória")
    @Future(message = "A data da consulta deve ser no futuro")
    private LocalDateTime dataConsulta;

    private String formaPagamento;
    private String nomeConvenio;
    private String numeroCarteirinha;

    // Getters e Setters
    public UUID getMedicoId() { return medicoId; }
    public void setMedicoId(UUID medicoId) { this.medicoId = medicoId; }
    public UUID getPacienteId() { return pacienteId; }
    public void setPacienteId(UUID pacienteId) { this.pacienteId = pacienteId; }
    public LocalDateTime getDataConsulta() { return dataConsulta; }
    public void setDataConsulta(LocalDateTime dataConsulta) { this.dataConsulta = dataConsulta; }
    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }
    public String getNomeConvenio() { return nomeConvenio; }
    public void setNomeConvenio(String nomeConvenio) { this.nomeConvenio = nomeConvenio; }
    public String getNumeroCarteirinha() { return numeroCarteirinha; }
    public void setNumeroCarteirinha(String numeroCarteirinha) { this.numeroCarteirinha = numeroCarteirinha; }
}