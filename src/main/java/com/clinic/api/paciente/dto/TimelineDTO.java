package com.clinic.api.paciente.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class TimelineDTO {
    private UUID id;
    private LocalDateTime data;
    private String tipo;      // "CONSULTA" ou "EXAME"
    private String titulo;    // Nome do médico ou Nome do exame
    private String descricao; // Resumo da consulta ou Tipo do documento
    private String linkArquivo; // Caso seja um exame, para o médico clicar e abrir

    public TimelineDTO(UUID id, LocalDateTime data, String tipo, String titulo, String descricao, String linkArquivo) {
        this.id = id;
        this.data = data;
        this.tipo = tipo;
        this.titulo = titulo;
        this.descricao = descricao;
        this.linkArquivo = linkArquivo;
    }

    // Getters
    public UUID getId() { return id; }
    public LocalDateTime getData() { return data; }
    public String getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getLinkArquivo() { return linkArquivo; }
}