package com.clinic.api.documento.dto;

import com.clinic.api.documento.Documento;
import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoResponse(
        UUID id,
        String nomeArquivo,
        String categoria, // EXAME, RECEITA
        String origem,    // MEDICO, PACIENTE
        LocalDateTime data,
        String linkDownload
) {
    public DocumentoResponse(Documento doc) {
        this(
                doc.getId(),
                doc.getNomeOriginal(),
                doc.getCategoria(),
                doc.getOrigem(),
                doc.getDataUpload(),
                // Gera a URL para a API de download
                "/documentos/" + doc.getId() + "/baixar"
        );
    }
}