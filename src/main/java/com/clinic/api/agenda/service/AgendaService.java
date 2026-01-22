package com.clinic.api.agenda.service;

import com.clinic.api.agenda.domain.*;
import com.clinic.api.agenda.dto.AgendaConfigRequest;
import com.clinic.api.agenda.dto.BloqueioRequest;
// import com.clinic.api.agendamento.domain.AgendamentoRepository; // <--- Descomentar depois
import com.clinic.api.medico.Medico;
import com.clinic.api.medico.domain.MedicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AgendaService {

    private final ConfiguracaoAgendaRepository configRepository;
    private final BloqueioAgendaRepository bloqueioRepository;
    private final MedicoRepository medicoRepository;
    // private final AgendamentoRepository agendamentoRepository; // <--- Injetar depois

    public AgendaService(ConfiguracaoAgendaRepository configRepository,
                         BloqueioAgendaRepository bloqueioRepository,
                         MedicoRepository medicoRepository) {
        this.configRepository = configRepository;
        this.bloqueioRepository = bloqueioRepository;
        this.medicoRepository = medicoRepository;
    }

    // --- 1. CONFIGURAR HORÁRIOS ---
    @Transactional
    public ConfiguracaoAgenda salvarConfiguracao(AgendaConfigRequest request) {
        Medico medico = medicoRepository.findById(request.medicoId())
                .orElseThrow(() -> new RuntimeException("Médico não encontrado."));

        ConfiguracaoAgenda config = configRepository.findByMedicoIdAndDiaSemana(request.medicoId(), request.diaSemana())
                .orElse(new ConfiguracaoAgenda());

        config.setMedico(medico);
        config.setDiaSemana(request.diaSemana());
        config.setHorarioInicio(request.horarioInicio());
        config.setHorarioFim(request.horarioFim());
        config.setAtivo(true);

        return configRepository.save(config);
    }

    // --- 2. BLOQUEIOS ---
    @Transactional
    public BloqueioAgenda criarBloqueio(BloqueioRequest request) {
        if (request.inicio().isAfter(request.fim())) {
            throw new RuntimeException("Início do bloqueio deve ser anterior ao fim.");
        }

        Medico medico = medicoRepository.findById(request.medicoId())
                .orElseThrow(() -> new RuntimeException("Médico não encontrado."));

        BloqueioAgenda bloqueio = new BloqueioAgenda(
                medico,
                request.inicio(),
                request.fim(),
                request.motivo()
        );

        return bloqueioRepository.save(bloqueio);
    }

    // --- 3. DISPONIBILIDADE ---
    public List<LocalDateTime> listarHorariosDisponiveis(UUID medicoId, LocalDate data) {
        List<LocalDateTime> horariosLivres = new ArrayList<>();

        if (data.isBefore(LocalDate.now())) return horariosLivres;

        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico não encontrado."));

        var configOpt = configRepository.findByMedicoIdAndDiaSemana(medicoId, data.getDayOfWeek());

        if (configOpt.isEmpty() || !configOpt.get().getAtivo()) {
            return horariosLivres;
        }

        ConfiguracaoAgenda config = configOpt.get();
        // Null Safety para duração da consulta
        int duracao = (medico.getDuracaoConsulta() != null) ? medico.getDuracaoConsulta() : 30;

        LocalDateTime slotAtual = LocalDateTime.of(data, config.getHorarioInicio());
        LocalDateTime fimDoDia = LocalDateTime.of(data, config.getHorarioFim());

        while (slotAtual.plusMinutes(duracao).isBefore(fimDoDia.plusMinutes(1))) {
            LocalDateTime fimSlot = slotAtual.plusMinutes(duracao);

            if (slotAtual.isAfter(LocalDateTime.now())) {

                // Validação de Bloqueios
                boolean bloqueado = !bloqueioRepository.findBloqueiosNoIntervalo(medicoId, slotAtual, fimSlot).isEmpty();

                // Validação de Agendamentos (MOCKADO POR ENQUANTO)
                boolean ocupado = false;
                // ocupado = agendamentoRepository.existsByMedicoIdAndDataConsulta(medicoId, slotAtual);

                if (!bloqueado && !ocupado) {
                    horariosLivres.add(slotAtual);
                }
            }
            slotAtual = fimSlot;
        }
        return horariosLivres;
    }
}