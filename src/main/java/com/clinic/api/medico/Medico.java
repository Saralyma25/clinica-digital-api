package com.clinic.api.medico;

import com.clinic.api.medico.enun.Especialidade;
import com.clinic.api.usuario.Usuario;
import com.clinic.api.usuario.domain.UserRole;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import java.util.Objects;

@Entity(name = "Medico")
@Table(name = "tb_medico")
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String nome;

    // CRM e Especialidade começam nulos para o "Cadastro Rápido"
    @Column(unique = true)
    private String crm;

    @Enumerated(EnumType.STRING)
    private Especialidade especialidade;

    @Column(name = "valor_consulta")
    private BigDecimal valorConsulta;

    @Column(name = "telefone_profissional")
    private String telefoneProfissional;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fim")
    private LocalTime horaFim;

    @Column(name = "duracao_consulta")
    private Integer duracaoConsulta;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "cadastro_completo")
    private Boolean cadastroCompleto = false; // Começa como FALSE no cadastro rápido

    @Column(name = "agenda_bloqueada")
    private Boolean agendaBloqueada = true;

    @Column(name = "data_cadastro", updatable = false)
    private LocalDateTime dataCadastro;

    public Medico() {}

    // Construtor específico para o seu exemplo: Nome e Usuário (E-mail)
    public Medico(Usuario usuario, String nome) {
        this.usuario = usuario;
        this.nome = nome;
        this.ativo = true;
        this.cadastroCompleto = false;
        this.agendaBloqueada = true;
    }

    @PrePersist
    public void prePersist() {
        if (this.dataCadastro == null) this.dataCadastro = LocalDateTime.now();
        if (this.ativo == null) this.ativo = true;
        if (this.cadastroCompleto == null) this.cadastroCompleto = false;
        if (this.agendaBloqueada == null) this.agendaBloqueada = true;
    }

    // --- Getters e Setters Manuais ---
    // (Mantenha os getters e setters que você já tem)

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCrm() { return crm; }
    public void setCrm(String crm) { this.crm = crm; }
    public Especialidade getEspecialidade() { return especialidade; }
    public void setEspecialidade(Especialidade especialidade) { this.especialidade = especialidade; }
    public BigDecimal getValorConsulta() { return valorConsulta; }
    public void setValorConsulta(BigDecimal valorConsulta) { this.valorConsulta = valorConsulta; }
    public String getTelefoneProfissional() { return telefoneProfissional; }
    public void setTelefoneProfissional(String telefoneProfissional) { this.telefoneProfissional = telefoneProfissional; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public Integer getDuracaoConsulta() { return duracaoConsulta; }
    public void setDuracaoConsulta(Integer duracaoConsulta) { this.duracaoConsulta = duracaoConsulta; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public Boolean getCadastroCompleto() { return cadastroCompleto; }
    public void setCadastroCompleto(Boolean cadastroCompleto) { this.cadastroCompleto = cadastroCompleto; }
    public Boolean getAgendaBloqueada() { return agendaBloqueada; }
    public void setAgendaBloqueada(Boolean agendaBloqueada) { this.agendaBloqueada = agendaBloqueada; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medico medico = (Medico) o;
        return Objects.equals(id, medico.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}