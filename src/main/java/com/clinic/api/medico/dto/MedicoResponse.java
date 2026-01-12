package com.clinic.api.medico.dto;

import com.clinic.api.medico.Medico;
import java.math.BigDecimal;
import java.util.UUID;

public class MedicoResponse {
    private UUID id;
    private String nome;
    private String crm;
    private String especialidade;
    private BigDecimal valorConsulta;

    // Construtor que converte Entidade -> DTO
    public MedicoResponse(Medico medico) {
        this.id = medico.getId();
        this.nome = medico.getNome();
        this.crm = medico.getCrm();
        this.especialidade = medico.getEspecialidade();
        this.valorConsulta = medico.getValorConsulta();
    }

    // Apenas Getters (Response é só leitura)
    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getCrm() { return crm; }
    public String getEspecialidade() { return especialidade; }
    public BigDecimal getValorConsulta() { return valorConsulta; }
}