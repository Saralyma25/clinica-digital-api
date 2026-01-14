package com.clinic.api.prontuario.dto;

import java.util.List;
import java.util.UUID;

public class FolhaDeRostoDTO {

    private UUID pacienteId;
    private String nome;
    private int idade;
    private String comorbidades; // Doenças preexistentes
    private String alergias;
    private String resumoIA; // Espaço para o DeepSeek preencher
    private List<ResumoAtendimentoDTO> ultimosAtendimentos;

    public FolhaDeRostoDTO(UUID pacienteId, String nome, int idade, String comorbidades,
                           String alergias, String resumoIA, List<ResumoAtendimentoDTO> atendimentos) {
        this.pacienteId = pacienteId;
        this.nome = nome;
        this.idade = idade;
        this.comorbidades = comorbidades;
        this.alergias = alergias;
        this.resumoIA = resumoIA;
        this.ultimosAtendimentos = atendimentos;
    }

    // --- Getters Manuais ---
    public UUID getPacienteId() { return pacienteId; }
    public String getNome() { return nome; }
    public int getIdade() { return idade; }
    public String getComorbidades() { return comorbidades; }
    public String getAlergias() { return alergias; }
    public String getResumoIA() { return resumoIA; }
    public List<ResumoAtendimentoDTO> getUltimosAtendimentos() { return ultimosAtendimentos; }
}