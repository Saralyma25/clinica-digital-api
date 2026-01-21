package com.clinic.api.documento;

import com.clinic.api.paciente.Paciente; // Import da raiz do Paciente
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Objects;

@Entity(name = "Documento")
@Table(name = "tb_documento")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nomeOriginal; // Ex: "hemograma.pdf"

    @Column(nullable = false)
    private String caminhoArquivo; // Caminho físico no servidor/bucket

    @Column(nullable = false)
    private String tipoContentType; // Ex: "application/pdf", "image/jpeg"

    @Column(nullable = false)
    private String categoria; // Ex: "EXAME", "RECEITA", "LAUDO"

    @Column(nullable = false)
    private String origem; // "MEDICO" ou "PACIENTE"

    private Boolean vistoPeloMedico; // Útil para alertas no Dashboard

    @Column(nullable = false)
    private LocalDateTime dataUpload;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @PrePersist
    public void prePersist() {
        if (this.dataUpload == null) this.dataUpload = LocalDateTime.now();
        if (this.vistoPeloMedico == null) this.vistoPeloMedico = false;
    }

    public Documento() {}

    public Documento(Paciente paciente, String nomeOriginal, String caminhoArquivo, String tipoContentType, String categoria, String origem) {
        this.paciente = paciente;
        this.nomeOriginal = nomeOriginal;
        this.caminhoArquivo = caminhoArquivo;
        this.tipoContentType = tipoContentType;
        this.categoria = categoria;
        this.origem = origem;
        // Se foi o médico que subiu, ele já viu. Se foi o paciente, o médico precisa ver.
        this.vistoPeloMedico = "MEDICO".equalsIgnoreCase(origem);
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNomeOriginal() { return nomeOriginal; }
    public void setNomeOriginal(String nomeOriginal) { this.nomeOriginal = nomeOriginal; }
    public String getCaminhoArquivo() { return caminhoArquivo; }
    public void setCaminhoArquivo(String caminhoArquivo) { this.caminhoArquivo = caminhoArquivo; }
    public String getTipoContentType() { return tipoContentType; }
    public void setTipoContentType(String tipoContentType) { this.tipoContentType = tipoContentType; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }
    public Boolean getVistoPeloMedico() { return vistoPeloMedico; }
    public void setVistoPeloMedico(Boolean vistoPeloMedico) { this.vistoPeloMedico = vistoPeloMedico; }
    public LocalDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Documento documento = (Documento) o;
        return Objects.equals(id, documento.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}