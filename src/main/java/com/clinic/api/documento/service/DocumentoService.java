package com.clinic.api.documento.service;

import com.clinic.api.documento.Documento;
import com.clinic.api.documento.domain.DocumentoRepository;
import com.clinic.api.documento.dto.DocumentoResponse;
import com.clinic.api.paciente.Paciente;

import com.clinic.api.paciente.domain.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentoService {

    private final DocumentoRepository repository;
    private final PacienteRepository pacienteRepository;

    // Define a pasta onde os arquivos serão salvos na raiz do projeto
    private final Path diretorioUploads = Paths.get("uploads");

    public DocumentoService(DocumentoRepository repository, PacienteRepository pacienteRepository) {
        this.repository = repository;
        this.pacienteRepository = pacienteRepository;

        // Garante que a pasta existe ao iniciar
        try {
            Files.createDirectories(diretorioUploads);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao inicializar diretório de uploads.", e);
        }
    }

    @Transactional
    public DocumentoResponse salvarDocumento(UUID pacienteId, String categoria, String origem, MultipartFile arquivo) {
        // 1. Validação do Paciente
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        try {
            // 2. Salva o arquivo no disco físico
            String nomeArquivoFisico = salvarNoDisco(arquivo);

            // 3. Salva os metadados no Banco de Dados
            Documento documento = new Documento(
                    paciente,
                    arquivo.getOriginalFilename(),
                    nomeArquivoFisico, // Caminho completo ou nome
                    arquivo.getContentType(),
                    categoria.toUpperCase(), // EXAME, RECEITA
                    origem.toUpperCase()     // MEDICO, PACIENTE
            );

            Documento salvo = repository.save(documento);
            return new DocumentoResponse(salvo);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar o upload do arquivo.", e);
        }
    }

    public List<DocumentoResponse> listarPorPaciente(UUID pacienteId) {
        return repository.findByPacienteIdOrderByDataUploadDesc(pacienteId)
                .stream()
                .map(DocumentoResponse::new)
                .collect(Collectors.toList());
    }

    public Documento buscarEntidadePorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado."));
    }

    // --- MÉTODOS AUXILIARES DE ARQUIVO ---

    private String salvarNoDisco(MultipartFile arquivo) throws IOException {
        // Gera um nome único para evitar sobrescrita (UUID + Nome Original)
        String nomeUnico = UUID.randomUUID() + "_" + arquivo.getOriginalFilename();
        Path caminhoDestino = diretorioUploads.resolve(nomeUnico);

        // Copia o arquivo para a pasta
        Files.copy(arquivo.getInputStream(), caminhoDestino, StandardCopyOption.REPLACE_EXISTING);

        return caminhoDestino.toString();
    }
}