package com.clinic.api.agendamento.service;

import com.clinic.api.agendamento.Agendamento;


import com.clinic.api.agendamento.domain.AgendamentoRepository;
import com.clinic.api.agendamento.domain.StatusAgendamento;
import com.clinic.api.agendamento.dto.AgendamentoRequest;
import com.clinic.api.agendamento.dto.AgendamentoResponse;
import com.clinic.api.agendamento.dto.AtendimentoDiarioDTO;
import com.clinic.api.medico.Medico;
import com.clinic.api.medico.domain.MedicoRepository;
import com.clinic.api.paciente.Paciente;
import com.clinic.api.paciente.domain.PacienteRepository;
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

    // --- 1. AGENDAR (O Grande Método) ---
    @Transactional
    public AgendamentoResponse agendar(AgendamentoRequest request) {
        // A. Validar se Médico e Paciente existem
        Medico medico = medicoRepository.findById(request.getMedicoId())
                .orElseThrow(() -> new RuntimeException("Médico não encontrado."));

        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        // B. Validação: O médico já tem agendamento neste horário?
        boolean medicoOcupado = repository.existsByMedicoIdAndDataConsulta(
                medico.getId(), request.getDataConsulta());

        if (medicoOcupado) {
            throw new RuntimeException("Este horário já está preenchido para o Dr(a). " + medico.getNome());
        }

        // C. Validação: O paciente já tem compromisso neste horário?
        boolean pacienteOcupado = repository.existsByPacienteIdAndDataConsultaAndStatusNot(
                paciente.getId(), request.getDataConsulta(), StatusAgendamento.CANCELADO_PACIENTE);

        if (pacienteOcupado) {
            throw new RuntimeException("O paciente já possui um agendamento neste horário.");
        }

        // D. Montar o Objeto
        Agendamento agendamento = new Agendamento();
        agendamento.setMedico(medico);
        agendamento.setPaciente(paciente);
        agendamento.setDataConsulta(request.getDataConsulta());
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setFormaPagamento(request.getFormaPagamento());

        // E. Regra de Negócio: Financeiro
        if ("CONVENIO".equalsIgnoreCase(request.getFormaPagamento())) {
            agendamento.setNomeConvenio(request.getNomeConvenio());
            agendamento.setNumeroCarteirinha(request.getNumeroCarteirinha());
            agendamento.setValorConsulta(BigDecimal.ZERO); // Convênio não cobra direto
            agendamento.setStatusPagamento("CONVENIO_APROVADO");
        } else {
            // Particular: Pega o valor do cadastro do médico
            agendamento.setValorConsulta(medico.getValorConsulta());
            agendamento.setStatusPagamento("PENDENTE");
        }

        repository.save(agendamento);
        return new AgendamentoResponse(agendamento);
    }

    // --- 2. LISTAGEM GERAL ---
    public List<Agendamento> listarTodos() {
        return repository.findAll();
    }

    // --- 3. BUSCAR POR ID ---
    public Agendamento buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado."));
    }

    // --- 4. AÇÕES DE FLUXO ---
    @Transactional
    public void confirmarAgendamento(UUID id) {
        Agendamento agendamento = buscarPorId(id);
        // Regra: Só pode confirmar se estiver AGENDADO
        if (agendamento.getStatus() == StatusAgendamento.AGENDADO) {
            // Aqui poderíamos ter um status CONFIRMADO, mas vamos manter simples por enquanto
            // Ou mudar para CONCLUIDO apenas após a consulta.
            // Para este exemplo, vamos assumir que não muda o status visual, apenas loga ou envia email.
        }
    }

    @Transactional
    public void cancelar(UUID id) {
        Agendamento agendamento = buscarPorId(id);
        agendamento.setStatus(StatusAgendamento.CANCELADO_PACIENTE);
        agendamento.setStatusPagamento("CANCELADO");
        repository.save(agendamento);
    }

    // --- 5. DASHBOARD DIÁRIO (Visão da Secretária) ---
    public List<AtendimentoDiarioDTO> listarAtendimentosDoDia(UUID medicoId, LocalDate data) {
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.atTime(LocalTime.MAX);

        List<Agendamento> agendamentos = repository.findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(
                medicoId, inicioDia, fimDia);

        return agendamentos.stream().map(a -> new AtendimentoDiarioDTO(
                a.getId(),
                a.getDataConsulta(),
                a.getPaciente().getNome(),
                a.getMedico().getEspecialidade().toString(),
                a.getStatus().name(),
                a.getStatusPagamento(),
                a.getNomeConvenio() != null ? "CONVÊNIO: " + a.getNomeConvenio() : "PARTICULAR"
        )).collect(Collectors.toList());
    }
}