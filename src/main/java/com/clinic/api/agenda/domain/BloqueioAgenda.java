package com.clinic.api.agenda.domain;

import com.clinic.api.medico.Medico;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Objects;

@Entity
@Table(name = "tb_bloqueio_agenda")
public class BloqueioAgenda {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Column(nullable = false)
    private LocalDateTime inicioBloqueio;

    @Column(nullable = false)
    private LocalDateTime fimBloqueio;

    private String motivo; // "Almoço", "Férias"

    public BloqueioAgenda() {}

    public BloqueioAgenda(Medico medico, LocalDateTime inicio, LocalDateTime fim, String motivo) {
        this.medico = medico;
        this.inicioBloqueio = inicio;
        this.fimBloqueio = fim;
        this.motivo = motivo;
    }

    // Getters e Setters
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BloqueioAgenda that = (BloqueioAgenda) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}