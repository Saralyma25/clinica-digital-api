package com.clinic.api.convenio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ConvenioRequest {

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O registro ANS é obrigatório")
    private String registroAns;

    @NotNull(message = "Informe os dias para pagamento")
    private Integer diasParaPagamento;

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getRegistroAns() { return registroAns; }
    public void setRegistroAns(String registroAns) { this.registroAns = registroAns; }
    public Integer getDiasParaPagamento() { return diasParaPagamento; }
    public void setDiasParaPagamento(Integer diasParaPagamento) { this.diasParaPagamento = diasParaPagamento; }
}