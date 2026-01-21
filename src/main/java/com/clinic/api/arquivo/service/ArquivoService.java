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
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class ArquivoService {

    // Define a pasta "uploads" na raiz do projeto
    private final Path diretorioArquivos;

    public ArquivoService() {
        // Pega o diretório atual de execução e adiciona a pasta "uploads"
        this.diretorioArquivos = Paths.get(System.getProperty("user.dir"))
                .resolve("uploads")
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.diretorioArquivos);
        } catch (IOException ex) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads.", ex);
        }
    }

    /**
     * Salva o arquivo no disco e retorna o NOME ÚNICO gerado.
     */
    public String salvarArquivo(MultipartFile arquivo) {
        // Limpa o nome do arquivo para evitar ataques de path traversal (../)
        String nomeOriginal = StringUtils.cleanPath(Objects.requireNonNull(arquivo.getOriginalFilename()));

        if (nomeOriginal.contains("..")) {
            throw new RuntimeException("Nome de arquivo inválido (contém sequência de diretório): " + nomeOriginal);
        }

        // Gera um nome único: UUID + _ + NomeOriginal (ex: 550e8400-e29b..._exame.pdf)
        String nomeUnico = UUID.randomUUID().toString() + "_" + nomeOriginal;

        try {
            // Resolve o caminho completo
            Path destino = this.diretorioArquivos.resolve(nomeUnico);

            // Copia (substituindo se existir por azar um igual)
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            return nomeUnico; // Retorna apenas o nome para salvar no banco (na entidade Documento)
        } catch (IOException ex) {
            throw new RuntimeException("Erro ao salvar arquivo " + nomeOriginal, ex);
        }
    }

    /**
     * Carrega o arquivo do disco como um Resource (para download).
     */
    public Resource carregarArquivo(String nomeArquivo) {
        try {
            Path caminhoArquivo = this.diretorioArquivos.resolve(nomeArquivo).normalize();
            Resource recurso = new UrlResource(caminhoArquivo.toUri());

            if (recurso.exists() || recurso.isReadable()) {
                return recurso;
            } else {
                throw new RuntimeException("Arquivo não encontrado ou ilegível: " + nomeArquivo);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Erro ao buscar caminho do arquivo: " + nomeArquivo, ex);
        }
    }

    // Método auxiliar caso precise do caminho completo (usado por logs ou auditoria)
    public String getCaminhoCompleto(String nomeArquivo) {
        return this.diretorioArquivos.resolve(nomeArquivo).toString();
    }
}