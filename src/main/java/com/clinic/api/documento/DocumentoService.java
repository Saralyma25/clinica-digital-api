package com.clinic.api.documento;

// Importação corrigida para apontar exatamente para onde seu ArquivoService está
import com.clinic.api.arquivo.service.ArquivoService;
import com.clinic.api.paciente.Paciente;
import com.clinic.api.paciente.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentoService {

    private final DocumentoRepository repository;
    private final ArquivoService arquivoService;
    private final PacienteRepository pacienteRepository;

    public DocumentoService(DocumentoRepository repository, ArquivoService arquivoService, PacienteRepository pacienteRepository) {
        this.repository = repository;
        this.arquivoService = arquivoService;
        this.pacienteRepository = pacienteRepository;
    }

    @Transactional
    public Documento salvarDocumento(UUID pacienteId, String tipoDocumento, MultipartFile arquivo) {
        // 1. Verifica se o paciente existe
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));

        // 2. Chama o seu ArquivoService para salvar no disco (na pasta uploads)
        String caminhoNoDisco = arquivoService.salvarArquivo(arquivo);

        // 3. Cria o registro no banco de dados com a origem "CLINICA"
        Documento documento = new Documento(
                arquivo.getOriginalFilename(),
                caminhoNoDisco,
                tipoDocumento,
                paciente,
                "CLINICA"
        );

        return repository.save(documento);
    }

    public List<Documento> listarPorPaciente(UUID pacienteId) {
        return repository.findByPacienteIdOrderByDataUploadDesc(pacienteId);
    }
}