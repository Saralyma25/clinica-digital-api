package com.clinic.api.convenio;

import com.clinic.api.plano.Plano; // Vai ficar vermelho até criarmos a classe Plano
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "Convenio")
@Table(name = "tb_convenio")
public class Convenio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nome; // Ex: Unimed, Bradesco

    @Column(name = "registro_ans", unique = true)
    private String registroAns; // Código oficial ANS

    @Column(name = "dias_pagamento")
    private Integer diasParaPagamento;

    @Column(nullable = false)
    private Boolean ativo = true;

    // Relacionamento: Um Convênio tem vários Planos
    // mappedBy = "convenio" significa que na classe Plano existe um campo chamado 'convenio'
    @OneToMany(mappedBy = "convenio", cascade = CascadeType.ALL)
    private List<Plano> planos = new ArrayList<>();

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
    public List<Plano> getPlanos() { return planos; }
    public void setPlanos(List<Plano> planos) { this.planos = planos; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Convenio convenio = (Convenio) o;
        return Objects.equals(id, convenio.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}