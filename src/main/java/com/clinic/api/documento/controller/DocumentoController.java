package com.clinic.api.documento.controller;

import com.clinic.api.documento.Documento;
import com.clinic.api.documento.dto.DocumentoResponse;
import com.clinic.api.documento.service.DocumentoService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/documentos")
public class DocumentoController {

    private final DocumentoService service;

    public DocumentoController(DocumentoService service) {
        this.service = service;
    }

    // --- UPLOAD ---
    // Exemplo: POST /documentos/upload?pacienteId=...&categoria=EXAME&origem=PACIENTE
    @PostMapping("/upload")
    public ResponseEntity<DocumentoResponse> upload(
            @RequestParam UUID pacienteId,
            @RequestParam String categoria, // Ex: "EXAME", "RECEITA"
            @RequestParam String origem,    // Ex: "MEDICO", "PACIENTE"
            @RequestParam("arquivo") MultipartFile arquivo) {

        DocumentoResponse response = service.salvarDocumento(pacienteId, categoria, origem, arquivo);
        return ResponseEntity.ok(response);
    }

    // --- LISTAGEM ---
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<DocumentoResponse>> listar(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(service.listarPorPaciente(pacienteId));
    }

    // --- DOWNLOAD ---
    @GetMapping("/{id}/baixar")
    public ResponseEntity<Resource> baixar(@PathVariable UUID id) {
        try {
            Documento documento = service.buscarEntidadePorId(id);
            Path caminhoArquivo = Paths.get(documento.getCaminhoArquivo());
            Resource resource = new UrlResource(caminhoArquivo.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(documento.getTipoContentType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + documento.getNomeOriginal() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}