package com.clinic.api.agenda.service;

import com.clinic.api.agenda.domain.*;
import com.clinic.api.agenda.dto.AgendaConfigRequest;
import com.clinic.api.agenda.dto.BloqueioRequest;
import com.clinic.api.agendamento.domain.AgendamentoRepository;
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
    private final AgendamentoRepository agendamentoRepository; // Injeção necessária

    public AgendaService(ConfiguracaoAgendaRepository configRepository,
                         BloqueioAgendaRepository bloqueioRepository,
                         MedicoRepository medicoRepository,
                         AgendamentoRepository agendamentoRepository) {
        this.configRepository = configRepository;
        this.bloqueioRepository = bloqueioRepository;
        this.medicoRepository = medicoRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    // --- 1. CONFIGURAR HORÁRIOS ---
    @Transactional
    public ConfiguracaoAgenda salvarConfiguracao(AgendaConfigRequest request) {
        // Valida se o médico existe
        Medico medico = medicoRepository.findById(request.medicoId())
                .orElseThrow(() -> new RuntimeException("Médico não encontrado."));

        // Busca config existente ou cria nova
        ConfiguracaoAgenda config = configRepository.findByMedicoIdAndDiaSemana(request.medicoId(), request.diaSemana())
                .orElse(new ConfiguracaoAgenda());

        // --- CORREÇÕES AQUI ---
        // 1. Usar setMedicoId (UUID) em vez de setMedico (Objeto)
        config.setMedicoId(request.medicoId());

        config.setDiaSemana(request.diaSemana());

        // 2. Mapear do DTO (horarioInicio) para a Entidade (horaInicio)
        config.setHoraInicio(request.horarioInicio());
        config.setHoraFim(request.horarioFim());

        // 3. Definir intervalo (Obrigatório para não gerar loop infinito)
        // Se o DTO não tem esse campo, pegamos do médico ou padrão 30min
        int duracao = (medico.getDuracaoConsulta() != null && medico.getDuracaoConsulta() > 0)
                ? medico.getDuracaoConsulta()
                : 30;
        config.setIntervaloMinutos(duracao);

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

        // Se a data já passou, não retorna nada
        if (data.isBefore(LocalDate.now())) return horariosLivres;

        // Verifica médico
        if (!medicoRepository.existsById(medicoId)) {
            throw new RuntimeException("Médico não encontrado.");
        }

        // Busca configuração do dia
        var configOpt = configRepository.findByMedicoIdAndDiaSemana(medicoId, data.getDayOfWeek());

        // 4. Usar isAtivo() em vez de getAtivo()
        if (configOpt.isEmpty() || !configOpt.get().isAtivo()) {
            return horariosLivres;
        }

        ConfiguracaoAgenda config = configOpt.get();

        // Proteção contra loop infinito
        if (config.getIntervaloMinutos() <= 0) config.setIntervaloMinutos(30);

        // 5. Usar getHoraInicio() da Entidade
        LocalDateTime slotAtual = LocalDateTime.of(data, config.getHoraInicio());
        LocalDateTime fimDoDia = LocalDateTime.of(data, config.getHoraFim());

        // Loop para gerar os slots
        while (slotAtual.isBefore(fimDoDia)) {
            LocalDateTime fimSlot = slotAtual.plusMinutes(config.getIntervaloMinutos());

            // Só adiciona se o horário for futuro
            if (slotAtual.isAfter(LocalDateTime.now())) {

                // Validação de Bloqueios
                boolean bloqueado = !bloqueioRepository.findBloqueiosNoIntervalo(medicoId, slotAtual, fimSlot).isEmpty();

                // Validação de Agendamentos (AGORA ATIVO)
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