package com.clinic.api.documento;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/documentos")
public class DocumentoController {

    private final com.clinic.api.documento.DocumentoService service;

    public DocumentoController(com.clinic.api.documento.DocumentoService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    public ResponseEntity<Documento> uploadDocumento(
            @RequestParam UUID pacienteId,
            @RequestParam String tipo,
            @RequestParam("arquivo") MultipartFile arquivo) {

        Documento salvo = service.salvarDocumento(pacienteId, tipo, arquivo);
        return ResponseEntity.ok(salvo);
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<Documento>> listarDocumentos(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(service.listarPorPaciente(pacienteId));
    }
}