package com.clinic.api.medico;


import com.clinic.api.clinica.Clinica;
import com.clinic.api.medico.enun.Especialidade;
import com.clinic.api.usuario.Usuario;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity(name = "Medico")
@Table(name = "tb_medico")
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", unique = true, nullable = false)
    private Usuario usuario;

    // --- NOVO RELACIONAMENTO: Um médico pertence a uma clínica ---
    @ManyToOne
    @JoinColumn(name = "clinica_id") // Cria a coluna clinica_id na tabela tb_medico
    private Clinica clinica;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true)
    private String crm;

    @Enumerated(EnumType.STRING)
    private Especialidade especialidade;

    private BigDecimal valorConsulta;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String telefoneProfissional;

    private Boolean cadastroCompleto = false;
    private Boolean agendaBloqueada = true; // Começa bloqueada até configurar
    private Boolean ativo = true;
    private LocalDateTime dataCadastro = LocalDateTime.now();

    // Configuração de Agenda Padrão (Simplificada)
    private Integer duracaoConsulta = 30; // minutos
    private LocalTime horaInicio = LocalTime.of(8, 0);
    private LocalTime horaFim = LocalTime.of(18, 0);

    // --- Construtores ---
    public Medico() {}

    public Medico(Usuario usuario, String nome) {
        this.usuario = usuario;
        this.nome = nome;
        this.cadastroCompleto = false;
        this.ativo = true;
        this.dataCadastro = LocalDateTime.now();
    }

    // --- Getters e Setters (Incluindo o novo setClinica) ---

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

    public Boolean getCadastroCompleto() { return cadastroCompleto; }
    public void setCadastroCompleto(Boolean cadastroCompleto) { this.cadastroCompleto = cadastroCompleto; }

    public Boolean getAgendaBloqueada() { return agendaBloqueada; }
    public void setAgendaBloqueada(Boolean agendaBloqueada) { this.agendaBloqueada = agendaBloqueada; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public Integer getDuracaoConsulta() { return duracaoConsulta; }
    public void setDuracaoConsulta(Integer duracaoConsulta) { this.duracaoConsulta = duracaoConsulta; }

    // --- GETTER E SETTER DA CLÍNICA (O que faltava) ---
    public Clinica getClinica() { return clinica; }
    public void setClinica(Clinica clinica) { this.clinica = clinica; }
}