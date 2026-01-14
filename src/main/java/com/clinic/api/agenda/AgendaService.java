package com.clinic.api.agenda;

import com.clinic.api.agendamento.AgendamentoRepository;
import com.clinic.api.medico.Medico;
import com.clinic.api.medico.MedicoRepository;
import org.springframework.stereotype.Service;

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

    // --- PARA O MÉDICO: Configurar sua agenda ---
    public ConfiguracaoAgenda salvarConfiguracao(ConfiguracaoAgenda config) {
        return configRepository.save(config);
    }

    public BloqueioAgenda criarBloqueio(BloqueioAgenda bloqueio) {
        // Ex: Bloquear dia 25/12 inteiro, ou bloquear almoço hoje
        return bloqueioRepository.save(bloqueio);
    }

    // --- PARA O PACIENTE: Ver horários disponíveis ---
    public List<LocalDateTime> listarHorariosDisponiveis(UUID medicoId, LocalDate data) {
        List<LocalDateTime> horariosLivres = new ArrayList<>();

        // 1. Busca os dados do médico para usar a duração personalizada
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico não encontrado."));

        // 2. Pega a configuração de jornada do médico para aquele dia da semana (ex: MONDAY)
        var configOpt = configRepository.findByMedicoIdAndDiaSemana(medicoId, data.getDayOfWeek());

        // Se o médico não configurou esse dia ou está marcado como inativo
        if (configOpt.isEmpty() || !configOpt.get().getAtivo()) {
            return horariosLivres; // Retorna lista vazia (Não atende neste dia)
        }

        ConfiguracaoAgenda config = configOpt.get();
        LocalTime inicioDia = config.getHorarioInicio(); // Ex: 08:00
        LocalTime fimDia = config.getHorarioFim();       // Ex: 18:00

        // Pega a duração da consulta definida no cadastro do médico (default 15 se nulo)
        int duracao = (medico.getDuracaoConsulta() != null) ? medico.getDuracaoConsulta() : 15;

        // 3. Gerador de Slots (Fatiador de tempo)
        LocalDateTime slotAtual = LocalDateTime.of(data, inicioDia);
        LocalDateTime limiteFim = LocalDateTime.of(data, fimDia);



        while (slotAtual.plusMinutes(duracao).isBefore(limiteFim.plusMinutes(1)) ) {
            LocalDateTime fimDoSlot = slotAtual.plusMinutes(duracao);

            // 4. Verifica se existe BLOQUEIO nesse intervalo (Almoço, Férias, etc.)
            // Usa a query personalizada do seu BloqueioAgendaRepository
            boolean temBloqueio = !bloqueioRepository.findBloqueiosNoIntervalo(medicoId, slotAtual, fimDoSlot).isEmpty();

            // 5. Verifica se já existe um AGENDAMENTO confirmado para este slot exato
            boolean jaAgendado = agendamentoRepository.existsByMedicoIdAndDataConsulta(medicoId, slotAtual);

            // 6. Se o horário não estiver bloqueado nem ocupado, ele é uma opção livre!
            if (!temBloqueio && !jaAgendado) {
                horariosLivres.add(slotAtual);
            }

            // Pula para o próximo slot de acordo com a duração da consulta
            slotAtual = slotAtual.plusMinutes(duracao);
        }

        return horariosLivres;
    }
}