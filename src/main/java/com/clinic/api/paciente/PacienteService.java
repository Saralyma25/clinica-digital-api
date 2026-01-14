package com.clinic.api.paciente;

import com.clinic.api.documento.DocumentoRepository;
import com.clinic.api.paciente.Paciente;
import com.clinic.api.paciente.PacienteRepository;
import com.clinic.api.paciente.dto.TimelineDTO;
import com.clinic.api.prontuario.ProntuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PacienteService {

    private final PacienteRepository repository;
    private final ProntuarioRepository prontuarioRepository;
    private final DocumentoRepository documentoRepository;

    // Construtor único que resolve o erro de inicialização (Autowire automático)
    public PacienteService(PacienteRepository repository,
                           ProntuarioRepository prontuarioRepository,
                           DocumentoRepository documentoRepository) {
        this.repository = repository;
        this.prontuarioRepository = prontuarioRepository;
        this.documentoRepository = documentoRepository;
    }

    public List<TimelineDTO> buscarTimelineCompleta(UUID pacienteId) {
        List<TimelineDTO> timeline = new ArrayList<>();

        // 1. Busca os Prontuários e transforma em eventos da Timeline
        prontuarioRepository.buscarHistoricoCompletoDoPaciente(pacienteId).forEach(p -> {
            timeline.add(new TimelineDTO(
                    p.getId(),
                    p.getAgendamento().getDataConsulta(),
                    "CONSULTA",
                    "Dr(a). " + p.getAgendamento().getMedico().getNome(),
                    p.getQueixaPrincipal() != null ? p.getQueixaPrincipal() : "Consulta de rotina",
                    null
            ));
        });

        // 2. Busca os Documentos (Uploads) e transforma em eventos da Timeline
        documentoRepository.findByPacienteIdOrderByDataUploadDesc(pacienteId).forEach(d -> {
            timeline.add(new TimelineDTO(
                    d.getId(),
                    d.getDataUpload(),
                    "EXAME",
                    d.getNomeOriginal(),
                    d.getTipo(),
                    d.getCaminhoArquivo()
            ));
        });

        // 3. Ordenação final: O mais recente aparece no topo
        return timeline.stream()
                .sorted(Comparator.comparing(TimelineDTO::getData).reversed())
                .collect(Collectors.toList());
    }

    // --- Métodos de Gestão de Paciente ---
    @Transactional
    public Paciente cadastrar(Paciente paciente) {
        return repository.save(paciente);
    }

    public List<Paciente> listarTodos() {
        return repository.findAll();
    }

    public Paciente buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));
    }
}