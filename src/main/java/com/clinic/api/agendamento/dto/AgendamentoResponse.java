package com.clinic.api.agendamento.dto;

import com.clinic.api.agendamento.Agendamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class AgendamentoResponse {

    private UUID id;
    private String nomeMedico;
    private String especialidade;
    private String nomePaciente;
    private LocalDateTime dataConsulta;
    private String status; // AGENDADO, CANCELADO, EM_PROCESSAMENTO
    private String statusPagamento;
    private BigDecimal valor;

    // --- Construtor que converte Entidade -> DTO ---
    public AgendamentoResponse(Agendamento agendamento) {
        this.id = agendamento.getId();
        // Null Safety: Verificamos se médico e paciente não são nulos para evitar erro 500
        if (agendamento.getMedico() != null) {
            this.nomeMedico = agendamento.getMedico().getNome();
            this.especialidade = agendamento.getMedico().getEspecialidade();
        }
        if (agendamento.getPaciente() != null) {
            this.nomePaciente = agendamento.getPaciente().getNome();
        }
        this.dataConsulta = agendamento.getDataConsulta();
        this.status = agendamento.getStatus();
        this.statusPagamento = agendamento.getStatusPagamento();
        this.valor = agendamento.getValorConsulta();
    }

    // --- Getters Manuais (Response geralmente só precisa de Getters) ---
    public UUID getId() { return id; }
    public String getNomeMedico() { return nomeMedico; }
    public String getEspecialidade() { return especialidade; }
    public String getNomePaciente() { return nomePaciente; }
    public LocalDateTime getDataConsulta() { return dataConsulta; }
    public String getStatus() { return status; }
    public String getStatusPagamento() { return statusPagamento; }
    public BigDecimal getValor() { return valor; }
}