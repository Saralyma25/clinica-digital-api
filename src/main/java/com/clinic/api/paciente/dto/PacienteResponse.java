package com.clinic.api.paciente.dto;

import com.clinic.api.paciente.Paciente;
import java.util.UUID;

public class PacienteResponse {
    private UUID id;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private Boolean cadastroCompleto; // <--- NOVO


    public PacienteResponse(Paciente paciente) {
        this.id = paciente.getId();
        this.nome = paciente.getNome();
        // Correção de NullPointerException caso usuario seja nulo (segurança)
        this.email = (paciente.getUsuario() != null) ? paciente.getUsuario().getEmail() : null;
        this.cpf = paciente.getCpf();
        this.telefone = paciente.getTelefone();
        this.cadastroCompleto = paciente.getCadastroCompleto();
    }

    // Getters
    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getCpf() { return cpf; }
    public String getTelefone() { return telefone; }
    public Boolean getCadastroCompleto() { return cadastroCompleto; }
}