package com.clinic.api.agenda.service;

import com.clinic.api.agenda.domain.*;
import com.clinic.api.agenda.dto.AgendaConfigRequest;
import com.clinic.api.agenda.dto.BloqueioRequest;
import com.clinic.api.agendamento.AgendamentoRepository;
import com.clinic.api.medico.Medico;
import com.clinic.api.medico.domain.MedicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AgendaService {

    private final ConfiguracaoAgendaRepository configRepository;
    private final BloqueioAgendaRepository bloqueioRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final MedicoRepository medicoRepository;

    public AgendaService(ConfiguracaoAgendaRepository configRepository,
                         BloqueioAgendaRepository bloqueioRepository,
                         AgendamentoRepository agendamentoRepository,
                         MedicoRepository medicoRepository) {
        this.configRepository = configRepository;
        this.bloqueioRepository = bloqueioRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.medicoRepository = medicoRepository;
    }

    // --- 1. CONFIGURAR HORÁRIOS DE TRABALHO ---
    @Transactional
    public ConfiguracaoAgenda salvarConfiguracao(AgendaConfigRequest request) {
        Medico medico = medicoRepository.findById(request.medicoId())
                .orElseThrow(() -> new RuntimeException("Médico não encontrado."));

        // Busca se já existe config para aquele dia (Update) ou cria nova (Insert)
        ConfiguracaoAgenda config = configRepository.findByMedicoIdAndDiaSemana(request.medicoId(), request.diaSemana())
                .orElse(new ConfiguracaoAgenda());

        config.setMedico(medico);
        config.setDiaSemana(request.diaSemana());
        config.setHorarioInicio(request.horarioInicio());
        config.setHorarioFim(request.horarioFim());
        config.setAtivo(true);

        return configRepository.save(config);
    }

    // --- 2. CRIAR BLOQUEIO (Almoço, Férias) ---
    @Transactional
    public BloqueioAgenda criarBloqueio(BloqueioRequest request) {
        if (request.inicio().isAfter(request.fim())) {
            throw new RuntimeException("A data de início do bloqueio deve ser anterior ao fim.");
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

    // --- 3. MOTOR DE DISPONIBILIDADE (Coração da Agenda) ---
    public List<LocalDateTime> listarHorariosDisponiveis(UUID medicoId, LocalDate data) {
        List<LocalDateTime> horariosLivres = new ArrayList<>();

        // Valida se o dia já passou
        if (data.isBefore(LocalDate.now())) return horariosLivres;

        // Recupera médico e config do dia
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico não encontrado."));

        var configOpt = configRepository.findByMedicoIdAndDiaSemana(medicoId, data.getDayOfWeek());

        // Se não trabalha nesse dia da semana
        if (configOpt.isEmpty() || !configOpt.get().getAtivo()) {
            return horariosLivres;
        }

        ConfiguracaoAgenda config = configOpt.get();
        int duracao = (medico.getDuracaoConsulta() != null) ? medico.getDuracaoConsulta() : 30; // Default seguro

        LocalDateTime slotAtual = LocalDateTime.of(data, config.getHorarioInicio());
        LocalDateTime fimDoDia = LocalDateTime.of(data, config.getHorarioFim());

        // Loop principal: Fatiador de Tempo
        while (slotAtual.plusMinutes(duracao).isBefore(fimDoDia.plusMinutes(1))) {
            LocalDateTime fimSlot = slotAtual.plusMinutes(duracao);

            // Regra: Só mostra horários futuros (se for hoje)
            if (slotAtual.isAfter(LocalDateTime.now())) {

                // Validação 1: Bloqueios (Almoço/Férias)
                boolean bloqueado = !bloqueioRepository.findBloqueiosNoIntervalo(medicoId, slotAtual, fimSlot).isEmpty();

                // Validação 2: Agendamentos Existentes
                boolean ocupado = agendamentoRepository.existsByMedicoIdAndDataConsulta(medicoId, slotAtual);

                if (!bloqueado && !ocupado) {
                    horariosLivres.add(slotAtual);
                }
            }
            slotAtual = fimSlot;
        }
        return horariosLivres;
    }
}