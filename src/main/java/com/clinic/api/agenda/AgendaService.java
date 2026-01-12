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

        // 1. Pega a configuração do médico para aquele dia da semana (ex: SEGUNDA)
        var configOpt = configRepository.findByMedicoIdAndDiaSemana(medicoId, data.getDayOfWeek());

        // Se o médico não configurou esse dia ou está marcado como inativo
        if (configOpt.isEmpty() || !configOpt.get().getAtivo()) {
            return horariosLivres; // Retorna lista vazia (Não atende hoje)
        }

        ConfiguracaoAgenda config = configOpt.get();
        LocalTime inicio = config.getHorarioInicio(); // 08:00
        LocalTime fim = config.getHorarioFim();       // 18:00

        // 2. Loop de 15 em 15 minutos para gerar os "slots"
        LocalDateTime slotAtual = LocalDateTime.of(data, inicio);
        LocalDateTime fimDoDia = LocalDateTime.of(data, fim);

        while (slotAtual.isBefore(fimDoDia)) {
            LocalDateTime fimDoSlot = slotAtual.plusMinutes(15); // Consulta dura 15min? Ou é só a grade?

            // 3. Verifica se tem BLOQUEIO nesse horário (Almoço, Férias)
            boolean temBloqueio = !bloqueioRepository.findBloqueiosNoIntervalo(medicoId, slotAtual, fimDoSlot).isEmpty();

            // 4. Verifica se já tem AGENDAMENTO (Paciente marcou)
            boolean jaAgendado = agendamentoRepository.existsByMedicoIdAndDataConsulta(medicoId, slotAtual);

            // Se não tem bloqueio NEM agendamento, está livre!
            if (!temBloqueio && !jaAgendado) {
                horariosLivres.add(slotAtual);
            }

            // Pula para o próximo slot de 15 min
            slotAtual = slotAtual.plusMinutes(15);
        }

        return horariosLivres;
    }
}