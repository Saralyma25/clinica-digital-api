package com.clinic.api.prontuario;

import com.clinic.api.paciente.Paciente;
import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Table(name = "tb_dados_clinicos_fixos")
@Entity(name = "DadosClinicosFixos")
public class DadosClinicosFixos {

    @Id
    private UUID pacienteId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @Column(columnDefinition = "TEXT")
    private String comorbidades; // Ex: Diabetes, Hipertensão

    @Column(columnDefinition = "TEXT")
    private String alergias; // Ponto Crítico!

    @Column(columnDefinition = "TEXT")
    private String observacoesPermanentes;

    // --- Construtores ---
    public DadosClinicosFixos() {}

    public DadosClinicosFixos(Paciente paciente, String comorbidades, String alergias) {
        this.paciente = paciente;
        this.pacienteId = paciente.getId();
        this.comorbidades = comorbidades;
        this.alergias = alergias;
    }

    // --- Getters e Setters (Manuais - Regra Mateus) ---
    public UUID getPacienteId() { return pacienteId; }
    public void setPacienteId(UUID pacienteId) { this.pacienteId = pacienteId; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public String getComorbidades() { return comorbidades; }
    public void setComorbidades(String comorbidades) { this.comorbidades = comorbidades; }

    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }

    public String getObservacoesPermanentes() { return observacoesPermanentes; }
    public void setObservacoesPermanentes(String observacoesPermanentes) { this.observacoesPermanentes = observacoesPermanentes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DadosClinicosFixos that = (DadosClinicosFixos) o;
        return Objects.equals(pacienteId, that.pacienteId);
    }

    @Override
    public int hashCode() { return Objects.hash(pacienteId); }
}