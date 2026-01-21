package com.clinic.api.plano.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class PlanoRequest {

    @NotBlank(message = "O nome do plano é obrigatório")
    private String nome;

    @NotNull(message = "O valor de repasse é obrigatório")
    private BigDecimal valorRepasse;

    @NotNull(message = "O ID do convênio é obrigatório")
    private UUID convenioId;

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BigDecimal getValorRepasse() { return valorRepasse; }
    public void setValorRepasse(BigDecimal valorRepasse) { this.valorRepasse = valorRepasse; }
    public UUID getConvenioId() { return convenioId; }
    public void setConvenioId(UUID convenioId) { this.convenioId = convenioId; }
}