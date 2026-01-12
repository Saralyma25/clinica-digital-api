package com.clinic.api.paciente;

import com.clinic.api.medico.Medico;
import com.clinic.api.plano.Plano;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Objects;

@Table(name = "tb_paciente")
@Entity(name = "Paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String telefone;

    @Column(length = 14)
    private String cpf;

    @ManyToOne
    @JoinColumn(name = "plano_id")
    private Plano plano;

    @Column(name = "numero_carteirinha")
    private String numeroCarteirinha;

    @Column(name = "validade_carteirinha")
    private LocalDate validadeCarteirinha;

    @Column(name = "atendimento_particular")
    private Boolean atendimentoParticular;

    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    // --- CONSTRUTOR OBRIGATÓRIO (O que faltava!) ---
    public Paciente() {
    }

    // Outros construtores úteis
    public Paciente(String nome, String email, String telefone, String cpf, Plano plano) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.cpf = cpf;
        this.plano = plano;
    }

    @PrePersist
    public void prePersist() {
        if(this.dataCadastro == null) this.dataCadastro = LocalDateTime.now();
    }

    // --- Getters e Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public Plano getPlano() { return plano; }
    public void setPlano(Plano plano) { this.plano = plano; }

    public String getNumeroCarteirinha() { return numeroCarteirinha; }
    public void setNumeroCarteirinha(String numeroCarteirinha) { this.numeroCarteirinha = numeroCarteirinha; }

    public LocalDate getValidadeCarteirinha() { return validadeCarteirinha; }
    public void setValidadeCarteirinha(LocalDate validadeCarteirinha) { this.validadeCarteirinha = validadeCarteirinha; }

    public Boolean getAtendimentoParticular() { return atendimentoParticular; }
    public void setAtendimentoParticular(Boolean atendimentoParticular) { this.atendimentoParticular = atendimentoParticular; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    // --- Equals e HashCode (Importantíssimo para o Banco) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Paciente paciente = (Paciente) o;
        return Objects.equals(id, paciente.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}