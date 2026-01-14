package com.clinic.api.agendamento.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class AtendimentoDiarioDTO {
    private UUID agendamentoId;
    private LocalDateTime horario;
    private String pacienteNome;
    private String especialidade;
    private String statusAtendimento; // AGENDADO, CONFIRMADO, CANCELADO
    private String statusPagamento;   // PAGO, PENDENTE, CONVENIO
    private String modalidade;        // PRESENCIAL, ONLINE

    public AtendimentoDiarioDTO(UUID agendamentoId, LocalDateTime horario, String pacienteNome,
                                String especialidade, String statusAtendimento,
                                String statusPagamento, String modalidade) {
        this.agendamentoId = agendamentoId;
        this.horario = horario;
        this.pacienteNome = pacienteNome;
        this.especialidade = especialidade;
        this.statusAtendimento = statusAtendimento;
        this.statusPagamento = statusPagamento;
        this.modalidade = modalidade;
    }

    // Getters Manuais
    public UUID getAgendamentoId() { return agendamentoId; }
    public LocalDateTime getHorario() { return horario; }
    public String getPacienteNome() { return pacienteNome; }
    public String getEspecialidade() { return especialidade; }
    public String getStatusAtendimento() { return statusAtendimento; }
    public String getStatusPagamento() { return statusPagamento; }
    public String getModalidade() { return modalidade; }
}