package com.clinic.api.paciente.dto;

import com.clinic.api.paciente.Paciente;
import java.util.UUID;

public class PacienteResponse {
    private UUID id;
    private String nome;
    private String email;
    private String cpf;

    public PacienteResponse(Paciente paciente) {
        this.id = paciente.getId();
        this.nome = paciente.getNome();
        this.email = paciente.getEmail();
        this.cpf = paciente.getCpf();
    }

    // Apenas Getters
    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getCpf() { return cpf; }
}