package com.clinic.api.convenio;

import jakarta.persistence.*;
import java.util.UUID;

@Entity(name = "Convenio")
@Table(name = "tb_convenio")
public class Convenio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(unique = true)
    private String registroAns;

    private Boolean ativo = true;

    // O CAMPO QUE ESTAVA DANDO ERRO
    private Integer diasPagamento;

    // --- Construtores ---
    public Convenio() {}

    public Convenio(String nome, String registroAns, Integer diasPagamento) {
        this.nome = nome;
        this.registroAns = registroAns;
        this.diasPagamento = diasPagamento;
        this.ativo = true;
    }

    // --- Getters e Setters ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getRegistroAns() { return registroAns; }
    public void setRegistroAns(String registroAns) { this.registroAns = registroAns; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    // --- AQUI ESTAVA FALTANDO ESTES DOIS ---
    public Integer getDiasPagamento() { return diasPagamento; }

    public void setDiasPagamento(Integer diasPagamento) {
        this.diasPagamento = diasPagamento;
    }
}