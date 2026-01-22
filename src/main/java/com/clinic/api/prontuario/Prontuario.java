package com.clinic.api.prontuario;

import com.clinic.api.agendamento.Agendamento;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Objects;

@Table(name = "tb_prontuario")
@Entity(name = "Prontuario")
public class Prontuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "agendamento_id", nullable = false, unique = true)
    private Agendamento agendamento;

    @Column(columnDefinition = "TEXT")
    private String queixaPrincipal;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Column(columnDefinition = "TEXT")
    private String prescricaoMedica;

    @Column(name = "data_registro")
    private LocalDateTime dataRegistro;

    public Prontuario() {}

    public Prontuario(Agendamento agendamento) {
        this.agendamento = agendamento;
    }

    @PrePersist
    public void prePersist() {
        if (this.dataRegistro == null) this.dataRegistro = LocalDateTime.now();
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Agendamento getAgendamento() { return agendamento; }
    public void setAgendamento(Agendamento agendamento) { this.agendamento = agendamento; }
    public String getQueixaPrincipal() { return queixaPrincipal; }
    public void setQueixaPrincipal(String queixaPrincipal) { this.queixaPrincipal = queixaPrincipal; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getPrescricaoMedica() { return prescricaoMedica; }
    public void setPrescricaoMedica(String prescricaoMedica) { this.prescricaoMedica = prescricaoMedica; }
    public LocalDateTime getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDateTime dataRegistro) { this.dataRegistro = dataRegistro; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prontuario that = (Prontuario) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}