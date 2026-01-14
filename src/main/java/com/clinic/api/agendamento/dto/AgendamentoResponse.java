package com.clinic.api.agendamento.dto;

import com.clinic.api.agendamento.Agendamento;
import com.clinic.api.medico.Especialidade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class AgendamentoResponse {

    private UUID id;
    private String nomeMedico;
    private Especialidade especialidade; // Corrigido: Removido ponto e vírgula extra
    private String nomePaciente;
    private LocalDateTime dataConsulta;
    private String status;
    private String statusPagamento;
    private BigDecimal valor;

    // --- Construtor que converte Entidade -> DTO ---
    public AgendamentoResponse(Agendamento agendamento) {
        this.id = agendamento.getId();

        // Null Safety: Verificamos se médico e paciente não são nulos
        if (agendamento.getMedico() != null) {
            this.nomeMedico = agendamento.getMedico().getNome();
            // Atribuição direta do Enum Especialidade vindo do Médico
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

    // --- Getters ---
    public UUID getId() { return id; }
    public String getNomeMedico() { return nomeMedico; }

    // CORREÇÃO: O retorno deve ser Especialidade (Enum) e não String
    public Especialidade getEspecialidade() { return especialidade; }

    public String getNomePaciente() { return nomePaciente; }
    public LocalDateTime getDataConsulta() { return dataConsulta; }
    public String getStatus() { return status; }
    public String getStatusPagamento() { return statusPagamento; }
    public BigDecimal getValor() { return valor; }
}