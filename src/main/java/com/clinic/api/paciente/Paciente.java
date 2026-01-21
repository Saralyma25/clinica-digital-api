package com.clinic.api.paciente; // Alterado para a raiz

import com.clinic.api.medico.Medico;
import com.clinic.api.plano.Plano;

import com.clinic.api.usuario.Usuario;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Objects;

@Entity(name = "Paciente")
@Table(name = "tb_paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    // O e-mail agora fica na tb_usuario, mas se você quiser manter um
    // e-mail de contato secundário aqui, pode. Caso contrário, usamos o do Usuario.
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;

    @Column(length = 14, unique = true)
    private String cpf;

    private String telefone;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "cadastro_completo")
    private Boolean cadastroCompleto = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id")
    private Plano plano;

    @Column(name = "numero_carteirinha")
    private String numeroCarteirinha;

    @Column(name = "validade_carteirinha")
    private LocalDate validadeCarteirinha;

    @Column(name = "atendimento_particular")
    private Boolean atendimentoParticular;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id")
    private Medico medico;

    @Column(name = "data_cadastro", updatable = false)
    private LocalDateTime dataCadastro;

    public Paciente() {}

    @PrePersist
    public void prePersist() {
        if(this.dataCadastro == null) this.dataCadastro = LocalDateTime.now();
        if(this.cadastroCompleto == null) this.cadastroCompleto = false;
        if(this.atendimentoParticular == null) this.atendimentoParticular = false;
    }

    // --- GETTERS E SETTERS MANUAIS (Padrão Sem Lombok) ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public Boolean getCadastroCompleto() { return cadastroCompleto; }
    public void setCadastroCompleto(Boolean cadastroCompleto) { this.cadastroCompleto = cadastroCompleto; }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Paciente paciente = (Paciente) o;
        return Objects.equals(id, paciente.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}