package com.clinic.api.prontuario;

import com.clinic.api.agendamento.AgendamentoRepository;
import com.clinic.api.paciente.Paciente;
import com.clinic.api.paciente.PacienteRepository;
import com.clinic.api.prontuario.dto.FolhaDeRostoDTO;
import com.clinic.api.prontuario.dto.ResumoAtendimentoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProntuarioService {

    private final ProntuarioRepository repository;
    private final AgendamentoRepository agendamentoRepository;
    private final DadosClinicosFixosRepository dadosFixosRepository;
    private final PacienteRepository pacienteRepository;
    private final DeepSeekService deepSeekService; // Injeção da IA

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

    // --- SALVAR (Folha de Atendimento com Trava de Escrita) ---
    @Transactional
    public Prontuario salvar(Prontuario prontuario, UUID idMedicoLogado) {
        // Validação de Segurança: Um médico não pode alterar folha de outro colega
        if (!prontuario.getAgendamento().getMedico().getId().equals(idMedicoLogado)) {
            throw new RuntimeException("Segurança: Você só pode registrar ou editar prontuários de seus próprios atendimentos.");
        }

        // Validação de Existência do Agendamento
        if (prontuario.getAgendamento() == null || !agendamentoRepository.existsById(prontuario.getAgendamento().getId())) {
            throw new RuntimeException("Agendamento inválido.");
        }

        // Trava de Unicidade: Cada consulta gera apenas uma folha no prontuário
        Optional<Prontuario> existente = repository.findByAgendamentoId(prontuario.getAgendamento().getId());
        if (existente.isPresent() && !existente.get().getId().equals(prontuario.getId())) {
            throw new RuntimeException("Já existe um prontuário para esta consulta.");
        }

        return repository.save(prontuario);
    }

    // --- A CAPA DA PASTA: Gerar Folha de Rosto com IA e Histórico ---
    public FolhaDeRostoDTO obterFolhaDeRosto(UUID pacienteId) {
        // 1. Busca os dados cadastrais do Paciente
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        // 2. Busca Dados Fixos (Comorbidades/Doenças Preexistentes e Alergias)
        DadosClinicosFixos dadosFixos = dadosFixosRepository.findById(pacienteId).orElse(null);

        // 3. Busca todas as folhas para compor o histórico e alimentar a IA
        List<Prontuario> todasAsFolhas = repository.buscarHistoricoCompletoDoPaciente(pacienteId);

        // 4. Monta o Histórico Simplificado (Top 10) para o médico
        List<ResumoAtendimentoDTO> historicoResumido = todasAsFolhas.stream()
                .limit(10)
                .map(p -> new ResumoAtendimentoDTO(
                        p.getAgendamento().getDataConsulta(),
                        p.getAgendamento().getMedico().getEspecialidade().toString(),
                        p.getAgendamento().getMedico().getNome()
                ))
                .collect(Collectors.toList());

        // 5. Prepara o texto e chama o DeepSeek para o resumo clínico inteligente
        String textoParaIA = todasAsFolhas.stream()
                .limit(10)
                .map(p -> "Data: " + p.getAgendamento().getDataConsulta() + " - Queixa: " + p.getQueixaPrincipal())
                .collect(Collectors.joining(" | "));

        String resumoIA = deepSeekService.gerarResumoClinico(textoParaIA);

        // 6. Cálculo dinâmico de Idade
        int idade = Period.between(paciente.getDataNascimento(), LocalDate.now()).getYears();

        // 7. Retorna o Consolidador (DTO) para o Front-end
        return new FolhaDeRostoDTO(
                paciente.getId(),
                paciente.getNome(),
                idade,
                (dadosFixos != null) ? dadosFixos.getComorbidades() : "Não informadas",
                (dadosFixos != null) ? dadosFixos.getAlergias() : "Sem alergias conhecidas",
                resumoIA,
                historicoResumido
        );
    }

    // --- OUTROS MÉTODOS DE CONSULTA (Acesso de Leitura Permitido) ---
    public List<Prontuario> listarHistoricoPaciente(UUID pacienteId) {
        return repository.buscarHistoricoCompletoDoPaciente(pacienteId);
    }

    public Prontuario buscarPorAgendamento(UUID agendamentoId) {
        return repository.findByAgendamentoId(agendamentoId)
                .orElseThrow(() -> new RuntimeException("Prontuário não encontrado."));
    }
}