package com.clinic.api.arquivo.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ArquivoService {

    // Define a pasta onde os arquivos ficarão salvos
    private final Path diretorioArquivos;

    public ArquivoService() {
        // Pega o caminho onde o projeto está rodando (C:\Users\Sara\IdeaProjects\clinica-digital-api)
        String pastaProjeto = System.getProperty("user.dir");

        // Define que salvaremos na subpasta "/uploads"
        this.diretorioArquivos = Paths.get(pastaProjeto, "uploads").toAbsolutePath().normalize();

        try {
            // Cria a pasta "uploads" se ela não existir
            Files.createDirectories(this.diretorioArquivos);
            System.out.println("📂 Pasta de Uploads configurada em: " + this.diretorioArquivos.toString());
        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível criar o diretório de upload!", ex);
        }
    }

    // --- SALVAR ARQUIVO ---
    public String salvarArquivo(MultipartFile arquivo) {
        // 1. Pega apenas o nome do arquivo, removendo qualquer caminho enviado pelo usuário
        String nomeOriginal = StringUtils.cleanPath(arquivo.getOriginalFilename());

        // 2. Defesa contra Path Traversal: impede o uso de ".."
        if (nomeOriginal.contains("..")) {
            throw new RuntimeException("Nome de arquivo inválido: " + nomeOriginal);
        }

        // 3. Gera o nome único com UUID
        String nomeUnico = UUID.randomUUID().toString() + "_" + nomeOriginal;

        try {
            Path destino = this.diretorioArquivos.resolve(nomeUnico);
            Files.copy(arquivo.getInputStream(), destino);
            return nomeUnico;
        } catch (IOException ex) {
            throw new RuntimeException("Erro ao salvar arquivo " + nomeOriginal, ex);
        }
    }

    // --- LER ARQUIVO (Download/Visualização) ---
    public Resource carregarArquivo(String nomeArquivo) {
        try {
            Path caminhoArquivo = this.diretorioArquivos.resolve(nomeArquivo).normalize();
            Resource recurso = new UrlResource(caminhoArquivo.toUri());

            if (recurso.exists()) {
                return recurso;
            } else {
                throw new RuntimeException("Arquivo não encontrado: " + nomeArquivo);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Arquivo não encontrado: " + nomeArquivo, ex);
        }
    }
}