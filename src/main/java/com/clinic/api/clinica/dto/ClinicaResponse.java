package com.clinic.api.clinica.dto;

// Importante: Importando do pacote domain correto
import com.clinic.api.clinica.Clinica;
import java.util.UUID;

public class ClinicaResponse {
    private UUID id;
    private String nomeFantasia;
    private String cnpj;
    private String telefone;
    private String endereco;

    public ClinicaResponse(Clinica clinica) {
        this.id = clinica.getId();
        this.nomeFantasia = clinica.getNomeFantasia();
        this.cnpj = clinica.getCnpj();
        this.telefone = clinica.getTelefone();
        this.endereco = clinica.getEndereco();
    }

    // Getters
    public UUID getId() { return id; }
    public String getNomeFantasia() { return nomeFantasia; }
    public String getCnpj() { return cnpj; }
    public String getTelefone() { return telefone; }
    public String getEndereco() { return endereco; }
}