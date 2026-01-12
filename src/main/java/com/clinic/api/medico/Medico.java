package com.clinic.api.medico;

import jakarta.persistence.*;
import java.math.BigDecimal; // Importante para dinheiro!
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Objects;

@Table(name = "tb_medico")
@Entity(name = "Medico")
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senha;

    private String crm;
    private String especialidade;

    // NOVO CAMPO: Valor da consulta particular
    @Column(name = "valor_consulta")
    private BigDecimal valorConsulta;

    private Boolean ativo;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    // --- Construtores ---
    public Medico() {}

    public Medico(String nome, String email, String senha, String crm, String especialidade, BigDecimal valorConsulta) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.crm = crm;
        this.especialidade = especialidade;
        this.valorConsulta = valorConsulta;
        this.ativo = true;
    }

    @PrePersist
    public void prePersist() {
        if (this.dataCadastro == null) this.dataCadastro = LocalDateTime.now();
        if (this.ativo == null) this.ativo = true;
    }

    // --- Getters e Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getCrm() { return crm; }
    public void setCrm(String crm) { this.crm = crm; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    // Getter e Setter do Valor
    public BigDecimal getValorConsulta() { return valorConsulta; }
    public void setValorConsulta(BigDecimal valorConsulta) { this.valorConsulta = valorConsulta; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    // --- Equals e HashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medico medico = (Medico) o;
        return Objects.equals(id, medico.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
