package com.clinic.api.plano;

import com.clinic.api.convenio.Convenio;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.Objects;

@Entity(name = "Plano")
@Table(name = "tb_plano")
public class Plano {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome; // Ex: "Enfermaria", "Apartamento", "VIP"

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "valor_repasse")
    private BigDecimal valorRepasse; // Quanto a clínica recebe por este plano

    @ManyToOne
    @JoinColumn(name = "convenio_id", nullable = false)
    private Convenio convenio;

    public Plano() {}

    // Construtor utilitário para o Service
    public Plano(String nome, BigDecimal valorRepasse, Convenio convenio) {
        this.nome = nome;
        this.valorRepasse = valorRepasse;
        this.convenio = convenio;
        this.ativo = true;
    }

    // --- Getters e Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public BigDecimal getValorRepasse() { return valorRepasse; }
    public void setValorRepasse(BigDecimal valorRepasse) { this.valorRepasse = valorRepasse; }
    public Convenio getConvenio() { return convenio; }
    public void setConvenio(Convenio convenio) { this.convenio = convenio; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plano plano = (Plano) o;
        return Objects.equals(id, plano.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}