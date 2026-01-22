package com.clinic.api.medico.service;


import com.clinic.api.clinica.Clinica;
import com.clinic.api.clinica.domain.ClinicaRepository;
import com.clinic.api.usuario.Usuario;
import com.clinic.api.medico.Medico;
import com.clinic.api.medico.domain.MedicoRepository;
import com.clinic.api.medico.dto.MedicoBasicoRequest;
import com.clinic.api.medico.dto.MedicoRequest;
import com.clinic.api.medico.dto.MedicoResponse;
import com.clinic.api.usuario.domain.UserRole;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MedicoService {

    private final MedicoRepository repository;
    private final ClinicaRepository clinicaRepository; // <--- INJEÇÃO NOVA

    // Injeção de dependência via construtor
    public MedicoService(MedicoRepository repository, ClinicaRepository clinicaRepository) {
        this.repository = repository;
        this.clinicaRepository = clinicaRepository;
    }

    // --- 1. CADASTRO RÁPIDO (Fluxo Google / Primeiro Acesso) ---
    @Transactional
    public MedicoResponse cadastrarRapido(MedicoBasicoRequest request) {
        if (repository.findByUsuarioEmail(request.email()).isPresent()) {
            throw new RuntimeException("Este e-mail já está cadastrado no sistema.");
        }

        // 1. Busca a Clínica (Validação de existência)
        Clinica clinica = clinicaRepository.findById(request.clinicaId())
                .orElseThrow(() -> new RuntimeException("Clínica não encontrada com o ID informado."));

        // 2. Cria Usuário
        Usuario usuario = new Usuario();
        usuario.setEmail(request.email());
        usuario.setSenha("123456");
        usuario.setRole(UserRole.MEDICO);
        usuario.setAtivo(true);

        // 3. Cria Médico e vincula a Clínica
        Medico medico = new Medico(usuario, request.nome());
        medico.setClinica(clinica); // <--- VÍNCULO FUNDAMENTAL

        Medico medicoSalvo = repository.save(medico);
        return new MedicoResponse(medicoSalvo);
    }

    // --- 2. CADASTRO COMPLETO (Fluxo Administrativo) ---
    @Transactional
    public MedicoResponse cadastrarCompleto(MedicoRequest request) {
        if (repository.findByUsuarioEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Este e-mail já está em uso.");
        }
        if (request.getCrm() != null && repository.existsByCrm(request.getCrm())) {
            throw new RuntimeException("Este CRM já está cadastrado.");
        }

        // 1. Busca a Clínica
        Clinica clinica = clinicaRepository.findById(request.getClinicaId())
                .orElseThrow(() -> new RuntimeException("Clínica não encontrada."));

        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setSenha("123456");
        usuario.setRole(UserRole.MEDICO);
        usuario.setAtivo(true);

        Medico medico = new Medico();
        medico.setUsuario(usuario);
        medico.setNome(request.getNome());
        medico.setCrm(request.getCrm());
        medico.setEspecialidade(request.getEspecialidade());
        medico.setValorConsulta(request.getValorConsulta());

        // Vínculo e Status
        medico.setClinica(clinica); // <--- VÍNCULO AQUI TAMBÉM
        medico.setCadastroCompleto(true);
        medico.setAgendaBloqueada(false);

        Medico medicoSalvo = repository.save(medico);
        return new MedicoResponse(medicoSalvo);
    }

    // --- LEITURAS (READ) ---
    public List<MedicoResponse> listarTodosAtivos() {
        return repository.findAll().stream()
                .filter(Medico::getAtivo)
                .map(MedicoResponse::new)
                .collect(Collectors.toList());
    }

    public MedicoResponse buscarPorId(UUID id) {
        Medico medico = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico não encontrado com ID: " + id));
        return new MedicoResponse(medico);
    }

    public MedicoResponse buscarPorCrm(String crm) {
        Medico medico = repository.findByCrm(crm)
                .orElseThrow(() -> new RuntimeException("Médico não encontrado com CRM: " + crm));
        return new MedicoResponse(medico);
    }

    public List<MedicoResponse> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome).stream()
                .map(MedicoResponse::new)
                .collect(Collectors.toList());
    }

    // --- ATUALIZAÇÃO (UPDATE) ---
    @Transactional
    public MedicoResponse atualizar(UUID id, MedicoRequest request) {
        Medico medico = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico não encontrado para atualização."));

        if (request.getNome() != null) medico.setNome(request.getNome());
        if (request.getCrm() != null) medico.setCrm(request.getCrm());
        if (request.getEspecialidade() != null) medico.setEspecialidade(request.getEspecialidade());
        if (request.getValorConsulta() != null) medico.setValorConsulta(request.getValorConsulta());

        // Se quiser permitir mudar de clínica na atualização:
        if (request.getClinicaId() != null) {
            Clinica novaClinica = clinicaRepository.findById(request.getClinicaId())
                    .orElseThrow(() -> new RuntimeException("Nova clínica não encontrada."));
            medico.setClinica(novaClinica);
        }

        if (medico.getCrm() != null && medico.getEspecialidade() != null) {
            medico.setCadastroCompleto(true);
        }

        Medico medicoAtualizado = repository.save(medico);
        return new MedicoResponse(medicoAtualizado);
    }

    // --- EXCLUSÃO LÓGICA (DELETE) ---
    @Transactional
    public void excluir(UUID id) {
        Medico medico = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico não encontrado para exclusão."));

        medico.setAtivo(false);
        repository.save(medico);
    }
}