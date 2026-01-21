package com.clinic.api.plano.dto;

import com.clinic.api.plano.Plano;
import java.math.BigDecimal;
import java.util.UUID;

public class PlanoResponse {
    private UUID id;
    private String nome;
    private BigDecimal valorRepasse;
    private Boolean ativo;
    private String nomeConvenio; // Útil para mostrar na tabela: "Unimed - Flex"

    public PlanoResponse(Plano plano) {
        this.id = plano.getId();
        this.nome = plano.getNome();
        this.valorRepasse = plano.getValorRepasse();
        this.ativo = plano.getAtivo();
        this.nomeConvenio = plano.getConvenio().getNome();
    }

    // Getters
    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public BigDecimal getValorRepasse() { return valorRepasse; }
    public Boolean getAtivo() { return ativo; }
    public String getNomeConvenio() { return nomeConvenio; }
}