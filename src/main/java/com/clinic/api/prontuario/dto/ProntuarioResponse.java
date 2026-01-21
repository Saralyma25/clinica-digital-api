package com.clinic.api.prontuario.dto;

import com.clinic.api.prontuario.Prontuario;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProntuarioResponse {
    private UUID id;
    private LocalDateTime dataConsulta;
    private String medicoNome;
    private String especialidade;
    private String queixaPrincipal;
    private String diagnostico;
    private String prescricaoMedica;

    public ProntuarioResponse(Prontuario prontuario) {
        this.id = prontuario.getId();
        this.dataConsulta = prontuario.getAgendamento().getDataConsulta();
        this.medicoNome = prontuario.getAgendamento().getMedico().getNome();
        this.especialidade = prontuario.getAgendamento().getMedico().getEspecialidade().toString();
        this.queixaPrincipal = prontuario.getQueixaPrincipal();
        this.diagnostico = prontuario.getDiagnostico();
        this.prescricaoMedica = prontuario.getPrescricaoMedica();
    }

    // Getters
    public UUID getId() { return id; }
    public LocalDateTime getDataConsulta() { return dataConsulta; }
    public String getMedicoNome() { return medicoNome; }
    public String getEspecialidade() { return especialidade; }
    public String getQueixaPrincipal() { return queixaPrincipal; }
    public String getDiagnostico() { return diagnostico; }
    public String getPrescricaoMedica() { return prescricaoMedica; }
}