package com.clinic.api.agenda;

import com.clinic.api.medico.Medico;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_bloqueio_agenda")
public class BloqueioAgenda {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    // Pode ser uma hora (12:00 as 13:00) ou um mês inteiro (01/01 as 31/01)
    @Column(nullable = false)
    private LocalDateTime inicioBloqueio;

    @Column(nullable = false)
    private LocalDateTime fimBloqueio;

    private String motivo; // "Almoço", "Férias", "Congresso"

    // Construtores, Getters e Setters...
    public BloqueioAgenda() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }
    public LocalDateTime getInicioBloqueio() { return inicioBloqueio; }
    public void setInicioBloqueio(LocalDateTime inicioBloqueio) { this.inicioBloqueio = inicioBloqueio; }
    public LocalDateTime getFimBloqueio() { return fimBloqueio; }
    public void setFimBloqueio(LocalDateTime fimBloqueio) { this.fimBloqueio = fimBloqueio; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}