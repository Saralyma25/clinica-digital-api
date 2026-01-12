package com.clinic.api.agendamento;

import com.clinic.api.medico.Medico;
import com.clinic.api.medico.MedicoRepository;
import com.clinic.api.paciente.Paciente;
import com.clinic.api.paciente.PacienteRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    // --- 1. O COMEÇO: Reservar a vaga (Status: EM_PROCESSAMENTO) ---
    public Agendamento agendar(Agendamento agendamento) {
        // 1. Validar se o Médico existe
        Medico medico = medicoRepository.findById(agendamento.getMedico().getId())
                .orElseThrow(() -> new RuntimeException("Médico não encontrado."));

        // 2. Validar se o Paciente existe
        Paciente paciente = pacienteRepository.findById(agendamento.getPaciente().getId())
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        // 3. TRAVA DE ESPECIALIDADE 🚫
        // Bloqueia se o paciente já tiver consulta "AGENDADO" ou "EM_PROCESSAMENTO" com essa especialidade
        boolean jaTemConsulta = repository.existsByPacienteIdAndMedico_EspecialidadeAndStatusNot(
                paciente.getId(),
                medico.getEspecialidade(),
                "CANCELADO"
        );

        if (jaTemConsulta) {
            throw new RuntimeException("Você já possui um agendamento em andamento com um " + medico.getEspecialidade() +
                    ". Finalize ou cancele o anterior.");
        }

        // 4. Regra de Horário 🕒
        if (repository.existsByMedicoIdAndDataConsulta(medico.getId(), agendamento.getDataConsulta())) {
            throw new RuntimeException("Horário indisponível (Já reservado).");
        }

        // 5. Validar Passado
        if (agendamento.getDataConsulta().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Não é possível agendar para o passado.");
        }

        // 6. Inteligência Financeira 💰 (CORREÇÃO DE SEGURANÇA AQUI) 👇
        // Usamos Boolean.TRUE.equals para evitar erro se o campo for nulo
        boolean ehParticular = Boolean.TRUE.equals(paciente.getAtendimentoParticular());

        if (paciente.getPlano() == null || ehParticular) {
            agendamento.setValorConsulta(medico.getValorConsulta());
            agendamento.setStatusPagamento("PENDENTE");
        } else {
            // É convênio
            agendamento.setValorConsulta(java.math.BigDecimal.ZERO);
            agendamento.setStatusPagamento("CONVENIO");
        }

        // 7. Configuração Final
        agendamento.setMedico(medico);
        agendamento.setPaciente(paciente);
        agendamento.setStatus("EM_PROCESSAMENTO"); // Status Temporário

        return repository.save(agendamento);
    }

    // --- 2. O FINAL: Confirmar o Agendamento ---
    public void confirmarAgendamento(UUID id) {
        Agendamento agendamento = buscarPorId(id);

        if (!agendamento.getStatus().equals("EM_PROCESSAMENTO")) {
            throw new RuntimeException("Este agendamento não está pendente de confirmação.");
        }

        agendamento.setStatus("AGENDADO"); // Oficializa
        repository.save(agendamento);
    }

    // --- Outros Métodos ---
    public List<Agendamento> listarTodos() {
        return repository.findAll();
    }

    public Agendamento buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado."));
    }

    public void cancelar(UUID id) {
        Agendamento agendamento = buscarPorId(id);
        agendamento.setStatus("CANCELADO");
        repository.save(agendamento);
    }

    // --- O FAXINEIRO (Robô) 🤖 ---
    @Scheduled(fixedRate = 60000) // Roda a cada 1 minuto
    @Transactional // OBRIGATÓRIO para o delete funcionar
    public void liberarHorariosTravados() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(15);
        repository.limparAgendamentosExpirados(limite);
        // System.out.println("⏰ Faxineiro: Limpeza realizada.");
    }
}