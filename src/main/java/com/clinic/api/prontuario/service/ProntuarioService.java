package com.clinic.api.prontuario.service;

import com.clinic.api.agendamento.Agendamento;
import com.clinic.api.agendamento.domain.AgendamentoRepository;
import com.clinic.api.agendamento.domain.StatusAgendamento;
import com.clinic.api.paciente.Paciente;
import com.clinic.api.paciente.domain.PacienteRepository;
import com.clinic.api.prontuario.domain.DadosClinicosFixos; // Import CORRETO
import com.clinic.api.prontuario.domain.DadosClinicosFixosRepository;
import com.clinic.api.prontuario.Prontuario; // Import CORRETO
import com.clinic.api.prontuario.domain.ProntuarioRepository;
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

    // 1. SALVAR ATENDIMENTO
    @Transactional
    public ProntuarioResponse salvar(ProntuarioRequest request, UUID idMedicoLogado) {
        Agendamento agendamento = agendamentoRepository.findById(request.getAgendamentoId())
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado."));

        // Validação de Segurança Básica
        if (!agendamento.getMedico().getId().equals(idMedicoLogado)) {
            throw new RuntimeException("Acesso negado: Médico incorreto.");
        }

        Prontuario prontuario = repository.findByAgendamentoId(request.getAgendamentoId())
                .orElse(new Prontuario(agendamento));

        prontuario.setQueixaPrincipal(request.getQueixaPrincipal());
        prontuario.setDiagnostico(request.getDiagnostico());
        prontuario.setPrescricaoMedica(request.getPrescricaoMedica());

        // Atualiza status do agendamento para REALIZADO
//        agendamento.setStatus("REALIZADO");
        agendamento.setStatus(StatusAgendamento.CONCLUIDO);
        // IMPORTANTE: Em produção, usar Enum StatusAgendamento.REALIZADO aqui

        agendamentoRepository.save(agendamento);

        return new ProntuarioResponse(repository.save(prontuario));
    }

    // 2. SALVAR DADOS FIXOS
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

    // 3. FOLHA DE ROSTO (CAPA INTELIGENTE)
    public FolhaDeRostoDTO obterFolhaDeRosto(UUID pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        DadosClinicosFixos dadosFixos = dadosFixosRepository.findById(pacienteId).orElse(null);
        List<Prontuario> historico = repository.buscarHistoricoCompletoDoPaciente(pacienteId);

        // --- INTEGRAÇÃO COM DEEPSEEK ---
        String resumoIA = "Sem histórico suficiente para análise.";
        if (historico.size() >= 1) { // Mudado para >= 1 para testar mais fácil
            String textoParaIA = historico.stream().limit(5)
                    .map(p -> "[Data: " + p.getAgendamento().getDataConsulta().toLocalDate() +
                            " | Diag: " + p.getDiagnostico() +
                            " | Remedios: " + p.getPrescricaoMedica() + "]")
                    .collect(Collectors.joining(" ;; "));

            resumoIA = deepSeekService.gerarResumoClinico(textoParaIA);
        }

        List<ResumoAtendimentoDTO> listaResumida = historico.stream().limit(10)
                .map(p -> new ResumoAtendimentoDTO(
                        p.getAgendamento().getDataConsulta(),
                        p.getAgendamento().getMedico().getEspecialidade().toString(),
                        p.getAgendamento().getMedico().getNome()
                ))
                .collect(Collectors.toList());

        int idade = 0;
        if(paciente.getDataNascimento() != null) {
            idade = Period.between(paciente.getDataNascimento(), LocalDate.now()).getYears();
        }

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

    public List<ProntuarioResponse> listarHistoricoPaciente(UUID pacienteId) {
        return repository.buscarHistoricoCompletoDoPaciente(pacienteId).stream()
                .map(ProntuarioResponse::new)
                .collect(Collectors.toList());
    }

    public ProntuarioResponse buscarPorAgendamento(UUID agendamentoId) {
        Prontuario prontuario = repository.findByAgendamentoId(agendamentoId)
                .orElseThrow(() -> new RuntimeException("Prontuário ainda não criado."));
        return new ProntuarioResponse(prontuario);
    }
}