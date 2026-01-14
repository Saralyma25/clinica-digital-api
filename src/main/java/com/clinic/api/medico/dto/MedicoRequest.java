package com.clinic.api.medico.dto;

import com.clinic.api.medico.Especialidade;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class MedicoRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "CRM é obrigatório")
    private String crm;

    @NotNull(message = "Especialidade é obrigatória")
    private Especialidade especialidade; // ALTERADO: De String para Especialidade

    @NotNull(message = "Valor da consulta é obrigatório")
    private BigDecimal valorConsulta;

    // --- Getters e Setters ---
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCrm() { return crm; }
    public void setCrm(String crm) { this.crm = crm; }

    // CORREÇÃO: O retorno agora é o Enum Especialidade
    public Especialidade getEspecialidade() { return especialidade; }
    public void setEspecialidade(Especialidade especialidade) { this.especialidade = especialidade; }

    public BigDecimal getValorConsulta() { return valorConsulta; }
    public void setValorConsulta(BigDecimal valorConsulta) { this.valorConsulta = valorConsulta; }
}