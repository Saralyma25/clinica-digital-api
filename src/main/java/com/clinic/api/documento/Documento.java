package com.clinic.api.documento;

import com.clinic.api.paciente.Paciente;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_documento")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nomeOriginal;

    @Column(nullable = false)
    private String caminhoArquivo;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String origem;

    private Boolean vistoPeloMedico;
    private LocalDateTime dataUpload;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @PrePersist
    public void prePersist() {
        this.dataUpload = LocalDateTime.now();
    }

    public Documento() {}

    // Construtor completo usado pelo Service
    public Documento(String nomeOriginal, String caminhoArquivo, String tipo, Paciente paciente, String origem) {
        this.nomeOriginal = nomeOriginal;
        this.caminhoArquivo = caminhoArquivo;
        this.tipo = tipo;
        this.paciente = paciente;
        this.origem = origem;
        this.vistoPeloMedico = "CLINICA".equals(origem);
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public String getNomeOriginal() { return nomeOriginal; }
    public String getCaminhoArquivo() { return caminhoArquivo; }
    public String getTipo() { return tipo; }
    public String getOrigem() { return origem; }
    public Boolean getVistoPeloMedico() { return vistoPeloMedico; }
    public void setVistoPeloMedico(Boolean vistoPeloMedico) { this.vistoPeloMedico = vistoPeloMedico; }
    public LocalDateTime getDataUpload() { return dataUpload; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
}