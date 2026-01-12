package com.clinic.api.plano;

import com.clinic.api.convenio.Convenio;
import jakarta.persistence.*;
import java.util.UUID;

@Table(name = "tb_plano")
@Entity(name = "Plano")
public class Plano {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome; // "Start", "Premium"

    // NOVO: Define se a clínica aceita este plano específico
    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "convenio_id", nullable = false)
    private Convenio convenio;

    public Plano() {}

    public Plano(String nome, Convenio convenio) {
        this.nome = nome;
        this.convenio = convenio;
        this.ativo = true;
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public Convenio getConvenio() { return convenio; }
    public void setConvenio(Convenio convenio) { this.convenio = convenio; }
}