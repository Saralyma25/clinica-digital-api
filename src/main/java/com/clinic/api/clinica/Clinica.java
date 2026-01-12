package com.clinic.api.clinica;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.Objects;

@Table(name = "tb_clinica")
@Entity(name = "Clinica")
public class Clinica {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String razaoSocial;

    private String nomeFantasia;

    @Column(unique = true, nullable = false)
    private String cnpj;

    private String endereco;
    private String telefone;

    // --- Construtores ---
    public Clinica() {}

    public Clinica(String razaoSocial, String cnpj) {
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
    }

    // --- Getters e Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    // --- Equals e HashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clinica clinica = (Clinica) o;
        return Objects.equals(id, clinica.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
