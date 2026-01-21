package com.clinic.api.paciente.service;

import com.clinic.api.documento.domain.DocumentoRepository;
import com.clinic.api.paciente.Paciente;
import com.clinic.api.paciente.domain.PacienteRepository;
import com.clinic.api.paciente.dto.PacienteBasicoRequest;
import com.clinic.api.paciente.dto.PacienteRequest;
import com.clinic.api.paciente.dto.PacienteResponse;
import com.clinic.api.paciente.dto.TimelineDTO;
import com.clinic.api.prontuario.domain.ProntuarioRepository;
import com.clinic.api.usuario.Usuario;
import com.clinic.api.usuario.domain.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PacienteService {

    private final PacienteRepository repository;
    private final ProntuarioRepository prontuarioRepository;
    private final DocumentoRepository documentoRepository;

    public PacienteService(PacienteRepository repository,
                           ProntuarioRepository prontuarioRepository,
                           DocumentoRepository documentoRepository) {
        this.repository = repository;
        this.prontuarioRepository = prontuarioRepository;
        this.documentoRepository = documentoRepository;
    }

    // --- CENÁRIO 1: Cadastro Rápido (Google) ---
    @Transactional
    public PacienteResponse cadastrarRapido(PacienteBasicoRequest request) {
        if (repository.existsByUsuarioEmail(request.email())) {
            throw new RuntimeException("Este e-mail já está cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(request.email());
        usuario.setSenha("123456");
        usuario.setRole(UserRole.PACIENTE);
        usuario.setAtivo(true);

        Paciente paciente = new Paciente();
        paciente.setUsuario(usuario);
        paciente.setNome(request.nome());
        paciente.setCadastroCompleto(false);

        Paciente salvo = repository.save(paciente);
        return new PacienteResponse(salvo);
    }

    // --- CENÁRIO 2: Cadastro Completo (Formulário) ---
    @Transactional
    public PacienteResponse cadastrarCompleto(PacienteRequest request) {
        if (repository.existsByUsuarioEmail(request.getEmail())) {
            throw new RuntimeException("Este e-mail já está cadastrado.");
        }
        if (repository.existsByCpf(request.getCpf())) {
            throw new RuntimeException("Este CPF já está cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setSenha("123456");
        usuario.setRole(UserRole.PACIENTE);
        usuario.setAtivo(true);

        Paciente paciente = new Paciente();
        paciente.setUsuario(usuario);
        paciente.setNome(request.getNome());
        paciente.setCpf(request.getCpf());
        paciente.setTelefone(request.getTelefone());
        paciente.setCadastroCompleto(true);

        Paciente salvo = repository.save(paciente);
        return new PacienteResponse(salvo);
    }

    // --- LEITURAS ---
    public List<PacienteResponse> listarTodos() {
        return repository.findAll().stream()
                .map(PacienteResponse::new)
                .collect(Collectors.toList());
    }

    public PacienteResponse buscarPorId(UUID id) {
        Paciente paciente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));
        return new PacienteResponse(paciente);
    }

    // --- TIMELINE DO PACIENTE ---
    public List<TimelineDTO> buscarTimelineCompleta(UUID pacienteId) {
        List<TimelineDTO> timeline = new ArrayList<>();

        // Adiciona Consultas (Prontuários)
        var prontuarios = prontuarioRepository.buscarHistoricoCompletoDoPaciente(pacienteId);
        if (prontuarios != null) {
            prontuarios.forEach(p -> timeline.add(new TimelineDTO(
                    p.getId(),
                    p.getAgendamento().getDataConsulta(),
                    "CONSULTA",
                    "Dr(a). " + p.getAgendamento().getMedico().getNome(),
                    p.getQueixaPrincipal(),
                    null
            )));
        }

        // Adiciona Documentos (Exames)
        var documentos = documentoRepository.findByPacienteIdOrderByDataUploadDesc(pacienteId);
        if (documentos != null) {
            documentos.forEach(d -> timeline.add(new TimelineDTO(
                    d.getId(),
                    d.getDataUpload(),
                    "EXAME", // Categoria
                    d.getNomeOriginal(),
                    d.getTipoContentType(), // <--- CORRIGIDO AQUI (era d.getTipo())
                    d.getCaminhoArquivo()
            )));
        }

        return timeline.stream()
                .sorted(Comparator.comparing(TimelineDTO::getData).reversed())
                .collect(Collectors.toList());
    }
}