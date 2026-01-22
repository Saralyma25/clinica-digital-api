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

    // --- CADASTRO RÁPIDO ---
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

    // --- CADASTRO COMPLETO ---
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
                .filter(p -> p.getUsuario().getAtivo()) // Filtra apenas ativos
                .map(PacienteResponse::new)
                .collect(Collectors.toList());
    }

    public PacienteResponse buscarPorId(UUID id) {
        Paciente paciente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));
        return new PacienteResponse(paciente);
    }

    // --- ATUALIZAÇÃO (NOVO) ---
    @Transactional
    public PacienteResponse atualizar(UUID id, PacienteRequest request) {
        Paciente paciente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        // Atualiza campos permitidos
        if (request.getNome() != null) paciente.setNome(request.getNome());
        if (request.getCpf() != null) paciente.setCpf(request.getCpf());
        if (request.getTelefone() != null) paciente.setTelefone(request.getTelefone());

        // Se atualizou dados cadastrais, marca como completo
        if (paciente.getCpf() != null && paciente.getTelefone() != null) {
            paciente.setCadastroCompleto(true);
        }

        return new PacienteResponse(repository.save(paciente));
    }

    // --- EXCLUSÃO (NOVO) ---
    @Transactional
    public void excluir(UUID id) {
        Paciente paciente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        // Desativa o paciente e o usuário de acesso
        paciente.getUsuario().setAtivo(false);
        repository.save(paciente);
    }

    // --- TIMELINE ---
    public List<TimelineDTO> buscarTimelineCompleta(UUID pacienteId) {
        List<TimelineDTO> timeline = new ArrayList<>();

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

        var documentos = documentoRepository.findByPacienteIdOrderByDataUploadDesc(pacienteId);
        if (documentos != null) {
            documentos.forEach(d -> timeline.add(new TimelineDTO(
                    d.getId(),
                    d.getDataUpload(),
                    "EXAME",
                    d.getNomeOriginal(),
                    d.getTipoContentType(),
                    d.getCaminhoArquivo()
            )));
        }

        return timeline.stream()
                .sorted(Comparator.comparing(TimelineDTO::getData).reversed())
                .collect(Collectors.toList());
    }
}