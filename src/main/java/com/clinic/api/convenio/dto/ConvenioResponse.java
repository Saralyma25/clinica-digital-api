package com.clinic.api.convenio.dto;

import com.clinic.api.convenio.Convenio;
import java.util.UUID;

public class ConvenioResponse {
    private UUID id;
    private String nome;
    private String registroAns;
    private Integer diasParaPagamento;

    public ConvenioResponse(Convenio convenio) {
        this.id = convenio.getId();
        this.nome = convenio.getNome();
        this.registroAns = convenio.getRegistroAns();
        this.diasParaPagamento = convenio.getDiasParaPagamento();
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getRegistroAns() { return registroAns; }
    public Integer getDiasParaPagamento() { return diasParaPagamento; }
}