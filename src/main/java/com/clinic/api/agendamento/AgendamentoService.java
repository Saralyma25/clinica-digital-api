package com.clinic.api.agendamento;

import com.clinic.api.medico.Medico;
import com.clinic.api.medico.MedicoRepository;
import com.clinic.api.paciente.Paciente;
import com.clinic.api.paciente.PacienteRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class AgendamentoService {

    private final AgendamentoRepository repository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;

    public AgendamentoService(AgendamentoRepository repository,
                              MedicoRepository medicoRepository,
                              PacienteRepository pacienteRepository) {
        this.repository = repository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    // --- 1. O AGENDAMENTO: Com Lógica de Bypass para Massa de Dados ---
    @Transactional
    public Agendamento agendar(Agendamento agendamento) {
        // 1. Validar existência das entidades
        Medico medico = medicoRepository.findById(agendamento.getMedico().getId())
                .orElseThrow(() -> new RuntimeException("Médico não encontrado."));

        Paciente paciente = pacienteRepository.findById(agendamento.getPaciente().getId())
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        // 3.a Validação: Horário indisponível para o MÉDICO (Você já tem esta)
        if (repository.existsByMedicoIdAndDataConsulta(medico.getId(), agendamento.getDataConsulta())) {
            throw new RuntimeException("Horário indisponível para este médico.");
        }

// 3.b NOVA TRAVA: O PACIENTE não pode ter dois agendamentos no mesmo horário (Choque de Agenda)
// Usamos o status diferente de CANCELADO para garantir que o paciente possa remarcar se cancelou a anterior
        boolean pacienteOcupado = repository.existsByPacienteIdAndDataConsultaAndStatusNot(
                paciente.getId(),
                agendamento.getDataConsulta(),
                "CANCELADO"
        );

        if (pacienteOcupado) {
            throw new RuntimeException("O paciente já possui um agendamento neste mesmo horário com outro profissional.");
        }


        // 2. Trava de Especialidade (Regra 1.1): Impede duplicidade ativa
        List<String> statusAtivos = List.of("EM_PROCESSAMENTO", "AGENDADO", "CONFIRMADO");
        boolean jaTemConsultaAtiva = repository.existsByPacienteIdAndMedico_EspecialidadeAndStatusIn(
                paciente.getId(),
                medico.getEspecialidade(),
                statusAtivos
        );

        if (jaTemConsultaAtiva) {
            throw new RuntimeException("Você já possui um agendamento ativo para " + medico.getEspecialidade() +
                    ". Cancele o atual antes de marcar um novo.");
        }

        // 3. Validações de Horário e Passado
        if (repository.existsByMedicoIdAndDataConsulta(medico.getId(), agendamento.getDataConsulta())) {
            throw new RuntimeException("Horário indisponível para este médico.");
        }

        if (agendamento.getDataConsulta().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Não é possível agendar para uma data retroativa.");
        }

        // --- 🚀 LÓGICA DE BYPASS (Sugestão Sara: Forçar AGENDADO para criar massa) ---
        boolean ehParticular = Boolean.TRUE.equals(paciente.getAtendimentoParticular());

        if (!ehParticular) {
            // FLUXO CONVÊNIO: Já nasce aprovado
            agendamento.setValorConsulta(BigDecimal.ZERO);
            agendamento.setStatusPagamento("CONVENIO_APROVADO");
            agendamento.setStatus("AGENDADO");
        } else {
            // FLUXO PARTICULAR: Hardcode temporário para gerar massa
            agendamento.setValorConsulta(medico.getValorConsulta());

            // Bypass: Forçamos AGENDADO para não cair no robô faxineiro
            agendamento.setStatus("AGENDADO");
            agendamento.setStatusPagamento("PAGAMENTO_SIMULADO_BYPASS");
        }

        // 4. Configuração de Auditoria Final e Salvamento
        agendamento.setMedico(medico);
        agendamento.setPaciente(paciente);
        agendamento.setDataCadastro(LocalDateTime.now());

        return repository.save(agendamento);
    }



    // --- 2. CONFIRMAÇÃO: Mantida para casos de fluxo EM_PROCESSAMENTO futuro ---
    @Transactional
    public void confirmarAgendamento(UUID id) {
        Agendamento agendamento = buscarPorId(id);

        if (!agendamento.getStatus().equals("EM_PROCESSAMENTO")) {
            throw new RuntimeException("Este agendamento já foi processado ou está cancelado.");
        }

        // Validação de Boleto (Regra 1.3.b)
        if ("BOLETO".equalsIgnoreCase(agendamento.getFormaPagamento())) {
            long horasAteConsulta = ChronoUnit.HOURS.between(LocalDateTime.now(), agendamento.getDataConsulta());
            if (horasAteConsulta < 48) {
                throw new RuntimeException("Pagamento via boleto exige 48h de antecedência.");
            }
        }

        agendamento.setStatus("AGENDADO");

        if (!"CONVENIO_APROVADO".equals(agendamento.getStatusPagamento())) {
            agendamento.setStatusPagamento("PAGO");
        }

        repository.save(agendamento);
    }

    // --- 3. MÉTODOS DE CONSULTA E CANCELAMENTO ---
    public List<Agendamento> listarTodos() {
        return repository.findAll();
    }

    public Agendamento buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado."));
    }

    @Transactional
    public void cancelar(UUID id) {
        Agendamento agendamento = buscarPorId(id);
        agendamento.setStatus("CANCELADO");
        repository.save(agendamento);
    }

    // --- 4. O FAXINEIRO (Limpeza Automática) 🤖 ---
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void liberarHorariosTravados() {
        // Remove apenas o que for rascunho (EM_PROCESSAMENTO) antigo
        LocalDateTime limite = LocalDateTime.now().minusMinutes(15);
        repository.deleteByStatusAndDataCadastroBefore("EM_PROCESSAMENTO", limite);



    }

}
