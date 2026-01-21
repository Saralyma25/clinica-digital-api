package com.clinic.api.prontuario.service;

import com.clinic.api.agendamento.Agendamento;
import com.clinic.api.agendamento.AgendamentoRepository;
import com.clinic.api.paciente.Paciente;
import com.clinic.api.paciente.domain.PacienteRepository;
import com.clinic.api.prontuario.*;
import com.clinic.api.prontuario.domain.DadosClinicosFixosRepository; // Import correto do domain
import com.clinic.api.prontuario.domain.ProntuarioRepository; // Import correto do domain
import com.clinic.api.prontuario.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProntuarioService {

    private final ProntuarioRepository repository;
    private final AgendamentoRepository agendamentoRepository;
    private final DadosClinicosFixosRepository dadosFixosRepository;
    private final PacienteRepository pacienteRepository;
    private final DeepSeekService deepSeekService;

    public ProntuarioService(ProntuarioRepository repository,
                             AgendamentoRepository agendamentoRepository,
                             DadosClinicosFixosRepository dadosFixosRepository,
                             PacienteRepository pacienteRepository,
                             DeepSeekService deepSeekService) {
        this.repository = repository;
        this.agendamentoRepository = agendamentoRepository;
        this.dadosFixosRepository = dadosFixosRepository;
        this.pacienteRepository = pacienteRepository;
        this.deepSeekService = deepSeekService;
    }

    // --- 1. SALVAR ATENDIMENTO ---
    @Transactional
    public ProntuarioResponse salvar(ProntuarioRequest request, UUID idMedicoLogado) {
        Agendamento agendamento = agendamentoRepository.findById(request.getAgendamentoId())
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado."));

        if (!agendamento.getMedico().getId().equals(idMedicoLogado)) {
            throw new RuntimeException("Acesso negado: Você só pode registrar prontuários de seus próprios atendimentos.");
        }

        Prontuario prontuario = repository.findByAgendamentoId(request.getAgendamentoId())
                .orElse(new Prontuario(agendamento));

        prontuario.setQueixaPrincipal(request.getQueixaPrincipal());
        prontuario.setDiagnostico(request.getDiagnostico());
        prontuario.setPrescricaoMedica(request.getPrescricaoMedica());

        agendamento.setStatus("REALIZADO");
        agendamentoRepository.save(agendamento);

        return new ProntuarioResponse(repository.save(prontuario));
    }

    // --- 2. SALVAR DADOS FIXOS ---
    @Transactional
    public void salvarDadosFixos(DadosClinicosRequest request) {
        Paciente paciente = pacienteRepository.findById(request.pacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        DadosClinicosFixos dados = dadosFixosRepository.findById(request.pacienteId())
                .orElse(new DadosClinicosFixos(paciente));

        dados.setAlergias(request.alergias());
        dados.setComorbidades(request.comorbidades());
        dados.setObservacoesPermanentes(request.observacoesPermanentes());

        dadosFixosRepository.save(dados);
    }

    // --- 3. FOLHA DE ROSTO ---
    public FolhaDeRostoDTO obterFolhaDeRosto(UUID pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        DadosClinicosFixos dadosFixos = dadosFixosRepository.findById(pacienteId).orElse(null);
        List<Prontuario> historico = repository.buscarHistoricoCompletoDoPaciente(pacienteId);

        String resumoIA = "Sem histórico suficiente para análise.";
        if (historico.size() >= 2) {
            String textoParaIA = historico.stream().limit(5)
                    .map(p -> "Data: " + p.getAgendamento().getDataConsulta() +
                            ". Diagnóstico: " + p.getDiagnostico() +
                            ". Prescrição: " + p.getPrescricaoMedica())
                    .collect(Collectors.joining(" | "));
            resumoIA = deepSeekService.gerarResumoClinico(textoParaIA);
        }

        List<ResumoAtendimentoDTO> listaResumida = historico.stream().limit(10)
                .map(p -> new ResumoAtendimentoDTO(
                        p.getAgendamento().getDataConsulta(),
                        p.getAgendamento().getMedico().getEspecialidade().toString(),
                        p.getAgendamento().getMedico().getNome()
                ))
                .collect(Collectors.toList());

        int idade = Period.between(paciente.getDataNascimento(), LocalDate.now()).getYears();

        return new FolhaDeRostoDTO(
                paciente.getId(),
                paciente.getNome(),
                idade,
                (dadosFixos != null) ? dadosFixos.getComorbidades() : "",
                (dadosFixos != null) ? dadosFixos.getAlergias() : "",
                resumoIA,
                listaResumida
        );
    }

    // --- 4. LISTAR HISTÓRICO COMPLETO (Método que faltava) ---
    public List<ProntuarioResponse> listarHistoricoPaciente(UUID pacienteId) {
        // Busca Entidades no Repository e converte para DTOs
        return repository.buscarHistoricoCompletoDoPaciente(pacienteId).stream()
                .map(ProntuarioResponse::new)
                .collect(Collectors.toList());
    }

    // --- 5. BUSCAR POR AGENDAMENTO ---
    public ProntuarioResponse buscarPorAgendamento(UUID agendamentoId) {
        Prontuario prontuario = repository.findByAgendamentoId(agendamentoId)
                .orElseThrow(() -> new RuntimeException("Prontuário ainda não criado para este agendamento."));
        return new ProntuarioResponse(prontuario);
    }
}