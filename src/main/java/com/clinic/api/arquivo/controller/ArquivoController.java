package com.clinic.api.arquivo.controller;

import com.clinic.api.arquivo.service.ArquivoService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

@RestController
@RequestMapping("/arquivos")
public class ArquivoController {

    private final ArquivoService service;

    public ArquivoController(ArquivoService service) {
        this.service = service;
    }

    // --- UPLOAD (POST) ---
    // Recebe um Form-Data com chave 'arquivo'
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadArquivo(@RequestParam("arquivo") MultipartFile arquivo) {
        String nomeSalvo = service.salvarArquivo(arquivo);

        // Retorna um JSON simples: { "caminho": "uuid_nome.pdf" }
        return ResponseEntity.ok(Map.of("caminho", nomeSalvo));
    }

    // --- DOWNLOAD / VISUALIZAÇÃO (GET) ---
    // Exemplo: GET /arquivos/a1b2c3d4_exame.pdf
    @GetMapping("/{nomeArquivo}")
    public ResponseEntity<Resource> lerArquivo(@PathVariable String nomeArquivo) throws IOException {
        Resource arquivo = service.carregarArquivo(nomeArquivo);

        // Tenta descobrir o tipo do arquivo (PDF, PNG, JPG) para o navegador abrir certo
        String contentType = Files.probeContentType(arquivo.getFile().toPath());
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + arquivo.getFilename() + "\"")
                .body(arquivo);
    }
}