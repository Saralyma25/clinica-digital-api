package com.clinic.api.convenio;

import com.clinic.api.plano.Plano; // Importante: Importar a classe Plano
import jakarta.persistence.*;
import java.util.List; // Importante para a lista
import java.util.UUID;
import java.util.Objects;
import java.util.ArrayList;

@Table(name = "tb_convenio")
@Entity(name = "Convenio")
public class Convenio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nome; // Ex: Unimed, Bradesco

    @Column(name = "registro_ans")
    private String registroAns; // Código oficial do convênio

    @Column(name = "dias_pagamento")
    private Integer diasParaPagamento; // Ex: Convênio paga em 30 dias

    private Boolean ativo;

    // --- A NOVIDADE: Lista de Planos ---
    // Isso diz: "Um Convênio tem vários Planos"
    // O 'cascade = ALL' significa: se eu apagar o Convênio, apaga os Planos dele também.
    @OneToMany(mappedBy = "convenio", cascade = CascadeType.ALL)
    private List<Plano> planos = new ArrayList<>();

    // --- Construtores ---
    public Convenio() {}

    public Convenio(String nome, String registroAns, Integer diasParaPagamento) {
        this.nome = nome;
        this.registroAns = registroAns;
        this.diasParaPagamento = diasParaPagamento;
        this.ativo = true;
    }

    // --- Getters e Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getRegistroAns() { return registroAns; }
    public void setRegistroAns(String registroAns) { this.registroAns = registroAns; }

    public Integer getDiasParaPagamento() { return diasParaPagamento; }
    public void setDiasParaPagamento(Integer diasParaPagamento) { this.diasParaPagamento = diasParaPagamento; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    // Getter e Setter da Lista de Planos
    public List<Plano> getPlanos() { return planos; }
    public void setPlanos(List<Plano> planos) { this.planos = planos; }

    // --- Equals e HashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Convenio convenio = (Convenio) o;
        return Objects.equals(id, convenio.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}