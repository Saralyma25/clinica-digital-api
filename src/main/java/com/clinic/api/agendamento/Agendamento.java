package com.clinic.api.agendamento;

import com.clinic.api.medico.Medico;
import com.clinic.api.paciente.Paciente;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Objects;

@Table(name = "tb_agendamento")
@Entity(name = "Agendamento")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(name = "data_consulta", nullable = false)
    private LocalDateTime dataConsulta;

    // --- FINANCEIRO ---
    @Column(name = "valor_consulta")
    private BigDecimal valorConsulta; // Se for particular, preenchemos. Se convênio, fica null ou 0.

    @Column(name = "link_pagamento")
    private String linkPagamento; // Aqui guardaremos a URL do Pix/Cartão

    @Column(name = "status_pagamento")
    private String statusPagamento; // PENDENTE, PAGO, CANCELADO
    // ------------------

    private String status; // AGENDADO, REALIZADO, CANCELADO

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @PrePersist // Preenche a hora automaticamente antes de salvar
    public void prePersist() {
        if(this.dataCadastro == null) this.dataCadastro = LocalDateTime.now();
    }

    // --- Construtores ---
    public Agendamento() {}

    public Agendamento(Medico medico, Paciente paciente, LocalDateTime dataConsulta) {
        this.medico = medico;
        this.paciente = paciente;
        this.dataConsulta = dataConsulta;
        this.status = "AGENDADO";
        this.statusPagamento = "PENDENTE";
    }


    // --- Getters e Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public LocalDateTime getDataConsulta() { return dataConsulta; }
    public void setDataConsulta(LocalDateTime dataConsulta) { this.dataConsulta = dataConsulta; }

    public BigDecimal getValorConsulta() { return valorConsulta; }
    public void setValorConsulta(BigDecimal valorConsulta) { this.valorConsulta = valorConsulta; }

    public String getLinkPagamento() { return linkPagamento; }
    public void setLinkPagamento(String linkPagamento) { this.linkPagamento = linkPagamento; }

    public String getStatusPagamento() { return statusPagamento; }
    public void setStatusPagamento(String statusPagamento) { this.statusPagamento = statusPagamento; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agendamento that = (Agendamento) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}