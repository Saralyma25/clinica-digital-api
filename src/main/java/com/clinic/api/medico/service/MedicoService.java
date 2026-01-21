package com.clinic.api.medico.service;

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

    // Injeção de dependência via construtor (Melhor prática Spring)
    public MedicoService(MedicoRepository repository) {
        this.repository = repository;
    }

    // --- 1. CADASTRO RÁPIDO (Fluxo Google / Primeiro Acesso) ---
    @Transactional
    public MedicoResponse cadastrarRapido(MedicoBasicoRequest request) {
        // Regra de Negócio: Não permite e-mails duplicados no sistema
        if (repository.findByUsuarioEmail(request.email()).isPresent()) {
            throw new RuntimeException("Este e-mail já está cadastrado no sistema.");
        }

        // Cria o Usuário de acesso (Regra de negócio: senha padrão inicial)
        Usuario usuario = new Usuario();
        usuario.setEmail(request.email());
        usuario.setSenha("123456"); // TODO: Futuramente substituir por gerador de senha ou OAuth
        usuario.setRole(UserRole.MEDICO);
        usuario.setAtivo(true);

        // Cria o Médico vinculado
        Medico medico = new Medico(usuario, request.nome());

        // Persiste no Banco de Dados Real
        Medico medicoSalvo = repository.save(medico);

        return new MedicoResponse(medicoSalvo);
    }

    // --- 2. CADASTRO COMPLETO (Fluxo Administrativo / Formulário) ---
    @Transactional
    public MedicoResponse cadastrarCompleto(MedicoRequest request) {
        // Regras de validação de unicidade
        if (repository.findByUsuarioEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Este e-mail já está em uso.");
        }
        if (request.getCrm() != null && repository.existsByCrm(request.getCrm())) {
            throw new RuntimeException("Este CRM já está cadastrado.");
        }

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

        // Regra: Cadastro manual completo já entra validado e com agenda liberada
        medico.setCadastroCompleto(true);
        medico.setAgendaBloqueada(false);

        Medico medicoSalvo = repository.save(medico);

        return new MedicoResponse(medicoSalvo);
    }

    // --- LEITURAS (READ) ---

    public List<MedicoResponse> listarTodosAtivos() {
        // Busca no banco e converte Entidade -> DTO
        return repository.findAll().stream()
                .filter(Medico::getAtivo) // Filtra apenas os ativos (Regra de Negócio)
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
        // 1. Busca a entidade real no banco
        Medico medico = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico não encontrado para atualização."));

        // 2. Atualiza os dados permitidos
        if (request.getNome() != null) medico.setNome(request.getNome());
        if (request.getCrm() != null) medico.setCrm(request.getCrm());
        if (request.getEspecialidade() != null) medico.setEspecialidade(request.getEspecialidade());
        if (request.getValorConsulta() != null) medico.setValorConsulta(request.getValorConsulta());

        // Regra de Negócio: Se preencheu dados vitais, o cadastro deixa de ser pendente
        if (medico.getCrm() != null && medico.getEspecialidade() != null) {
            medico.setCadastroCompleto(true);
        }

        // 3. Salva as alterações
        Medico medicoAtualizado = repository.save(medico);

        return new MedicoResponse(medicoAtualizado);
    }

    // --- EXCLUSÃO LÓGICA (DELETE) ---
    @Transactional
    public void excluir(UUID id) {
        Medico medico = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico não encontrado para exclusão."));

        // Regra de Negócio: Não deletamos fisicamente, apenas inativamos
        medico.setAtivo(false);
        repository.save(medico);
    }
}