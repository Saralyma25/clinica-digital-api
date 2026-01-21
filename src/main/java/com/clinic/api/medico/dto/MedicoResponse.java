package com.clinic.api.medico.dto;

import com.clinic.api.medico.enun.Especialidade;
import com.clinic.api.medico.Medico;
import java.math.BigDecimal;
import java.util.UUID;

public class MedicoResponse {
    private UUID id;
    private String nome;
    private String email; // Adicionado para facilitar o Front
    private String crm;
    private Especialidade especialidade;
    private BigDecimal valorConsulta;
    private Boolean cadastroCompleto; // Adicionado para controle de fluxo

    public MedicoResponse(Medico medico) {
        this.id = medico.getId();
        this.nome = medico.getNome();
        // Buscamos o email do objeto Usuario vinculado
        this.email = medico.getUsuario() != null ? medico.getUsuario().getEmail() : null;
        this.crm = medico.getCrm();
        this.especialidade = medico.getEspecialidade();
        this.valorConsulta = medico.getValorConsulta();
        this.cadastroCompleto = medico.getCadastroCompleto();
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getCrm() { return crm; }
    public Especialidade getEspecialidade() { return especialidade; }
    public BigDecimal getValorConsulta() { return valorConsulta; }
    public Boolean getCadastroCompleto() { return cadastroCompleto; }
    public Boolean isCadastroCompleto() { return cadastroCompleto; }
}