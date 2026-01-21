package com.clinic.api.agendamento.service;

import com.clinic.api.agendamento.Agendamento;
import com.clinic.api.agendamento.AgendamentoRepository;
import com.clinic.api.agendamento.dto.AtendimentoDiarioDTO;
import com.clinic.api.medico.Medico;
import com.clinic.api.medico.domain.MedicoRepository;
import com.clinic.api.paciente.Paciente;

import com.clinic.api.paciente.domain.PacienteRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Transactional
    public Agendamento agendar(Agendamento agendamento) {
        // 1. Busca e Valida Médico e Paciente
        Medico medico = medicoRepository.findById(agendamento.getMedico().getId())
                .orElseThrow(() -> new RuntimeException("Médico não encontrado."));

        Paciente paciente = pacienteRepository.findById(agendamento.getPaciente().getId())
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        // 2. Validações de Agenda (Médico e Paciente ocupados?)
        if (repository.existsByMedicoIdAndDataConsulta(medico.getId(), agendamento.getDataConsulta())) {
            throw new RuntimeException("Horário indisponível para este médico.");
        }
        if (repository.existsByPacienteIdAndDataConsultaAndStatusNot(paciente.getId(), agendamento.getDataConsulta(), "CANCELADO")) {
            throw new RuntimeException("Paciente já possui agendamento neste horário.");
        }
        if (agendamento.getDataConsulta().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Não é possível agendar em datas passadas.");
        }

        // 3. Regra de Negócio: Definição de Preço (Particular vs Convênio)
        boolean ehParticular = Boolean.TRUE.equals(paciente.getAtendimentoParticular());

        if (ehParticular) {
            // FLUXO PARTICULAR: Usa o valor definido no perfil do Médico
            agendamento.setValorConsulta(medico.getValorConsulta());
            agendamento.setStatusPagamento("PENDENTE");
            agendamento.setNomeConvenio("PARTICULAR");
        } else {
            // FLUXO CONVÊNIO: Usa o valor de repasse do Plano do Paciente
            if (paciente.getPlano() == null) {
                throw new RuntimeException("Paciente marcado como Convênio, mas não possui Plano vinculado.");
            }

            BigDecimal valorRepasse = paciente.getPlano().getValorRepasse();
            agendamento.setValorConsulta(valorRepasse != null ? valorRepasse : BigDecimal.ZERO);

            // Puxa os dados do plano automaticamente para o histórico
            agendamento.setNomeConvenio(paciente.getPlano().getConvenio().getNome() + " - " + paciente.getPlano().getNome());
            agendamento.setStatusPagamento("CONVENIO_APROVADO"); // Convênio não paga na hora
        }

        // 4. Finalização
        agendamento.setStatus("AGENDADO");
        agendamento.setMedico(medico);
        agendamento.setPaciente(paciente);

        return repository.save(agendamento);
    }

    public List<Agendamento> listarTodos() {
        return repository.findAll();
    }

    public Agendamento buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado."));
    }

    public List<AtendimentoDiarioDTO> listarAtendimentosDoDia(UUID medicoId, LocalDate data) {
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.atTime(LocalTime.MAX);

        return repository.findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(medicoId, inicio, fim)
                .stream()
                .map(a -> new AtendimentoDiarioDTO(
                        a.getId(),
                        a.getDataConsulta(),
                        a.getPaciente().getNome(),
                        a.getMedico().getEspecialidade().toString(),
                        a.getStatus(),
                        a.getStatusPagamento(),
                        "PRESENCIAL"
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelar(UUID id) {
        Agendamento agendamento = buscarPorId(id);
        agendamento.setStatus("CANCELADO");
        repository.save(agendamento);
    }

    @Transactional
    public void confirmarAgendamento(UUID id) {
        Agendamento agendamento = buscarPorId(id);
        agendamento.setStatus("CONFIRMADO");
        if (!"CONVENIO_APROVADO".equals(agendamento.getStatusPagamento())) {
            agendamento.setStatusPagamento("PAGO");
        }
        repository.save(agendamento);
    }

    // Faxineiro: Limpa agendamentos que ficaram como rascunho (EM_PROCESSAMENTO) há mais de 15min
    @Scheduled(fixedRate = 600000) // Roda a cada 10 min
    @Transactional
    public void limparRascunhos() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(15);
        repository.deleteByStatusAndDataCadastroBefore("EM_PROCESSAMENTO", limite);
    }
}