package com.clinic.api.agenda;

import com.clinic.api.medico.Medico;
import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "tb_configuracao_agenda")
public class ConfiguracaoAgenda {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DayOfWeek diaSemana; // MONDAY, TUESDAY, etc.

    @Column(name = "horario_inicio")
    private LocalTime horarioInicio; // Ex: 08:00

    @Column(name = "horario_fim")
    private LocalTime horarioFim; // Ex: 18:00

    @Column(name = "ativo")
    private Boolean ativo = true; // Se false, ele não atende nesse dia da semana

    // Construtores, Getters e Setters
    public ConfiguracaoAgenda() {}

    public ConfiguracaoAgenda(Medico medico, DayOfWeek diaSemana, LocalTime inicio, LocalTime fim) {
        this.medico = medico;
        this.diaSemana = diaSemana;
        this.horarioInicio = inicio;
        this.horarioFim = fim;
    }

    // Gere os Getters e Setters...
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }
    public DayOfWeek getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DayOfWeek diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHorarioInicio() { return horarioInicio; }
    public void setHorarioInicio(LocalTime horarioInicio) { this.horarioInicio = horarioInicio; }
    public LocalTime getHorarioFim() { return horarioFim; }
    public void setHorarioFim(LocalTime horarioFim) { this.horarioFim = horarioFim; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}