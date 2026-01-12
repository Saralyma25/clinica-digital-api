package com.clinic.api.agenda;

import com.clinic.api.agendamento.AgendamentoRepository;
import com.clinic.api.medico.Medico;
import com.clinic.api.medico.MedicoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

    @InjectMocks
    private AgendaService agendaService;

    @Mock
    private ConfiguracaoAgendaRepository configRepository;

    @Mock
    private BloqueioAgendaRepository bloqueioRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @Test
    @DisplayName("❌ Deve retornar lista vazia se o médico NÃO trabalha no dia da semana")
    void deveRetornarVazioSeSemConfiguracao() {
        // Cenario
        UUID medicoId = UUID.randomUUID();
        LocalDate segundaFeira = LocalDate.of(2025, 1, 13); // É uma segunda-feira

        // Mock: O banco diz que NÃO achou configuração para Segunda-feira
        Mockito.when(configRepository.findByMedicoIdAndDiaSemana(eq(medicoId), eq(DayOfWeek.MONDAY)))
                .thenReturn(Optional.empty());

        // Ação
        List<LocalDateTime> horarios = agendaService.listarHorariosDisponiveis(medicoId, segundaFeira);

        // Verificação
        Assertions.assertTrue(horarios.isEmpty(), "A lista deveria estar vazia pois o médico não trabalha hoje");
    }

    @Test
    @DisplayName("✅ Deve gerar slots corretamente descontando AGENDAMENTOS e BLOQUEIOS")
    void deveGerarGradeDescontandoOcupacoes() {
        // --- CENÁRIO ---
        UUID medicoId = UUID.randomUUID();
        LocalDate data = LocalDate.of(2025, 1, 13); // Segunda-feira

        // 1. Configuração: Atende das 08:00 às 09:00 (Deve gerar 4 slots: 08:00, 08:15, 08:30, 08:45)
        ConfiguracaoAgenda config = new ConfiguracaoAgenda();
        config.setAtivo(true);
        config.setHorarioInicio(LocalTime.of(8, 0));
        config.setHorarioFim(LocalTime.of(9, 0));

        Mockito.when(configRepository.findByMedicoIdAndDiaSemana(eq(medicoId), eq(DayOfWeek.MONDAY)))
                .thenReturn(Optional.of(config));

        // 2. Bloqueio: Vamos simular um bloqueio às 08:15 (Ex: Reunião rápida)
        // O mock deve retornar uma lista NÃO vazia quando perguntarem do horário das 08:15
        LocalDateTime slotBloqueado = LocalDateTime.of(data, LocalTime.of(8, 15));

        Mockito.when(bloqueioRepository.findBloqueiosNoIntervalo(eq(medicoId), eq(slotBloqueado), any()))
                .thenReturn(List.of(new BloqueioAgenda())); // Retorna algo para dizer "Tem bloqueio"

        // Para os outros horários, retorna lista vazia (sem bloqueio)
        Mockito.when(bloqueioRepository.findBloqueiosNoIntervalo(eq(medicoId),
                        org.mockito.ArgumentMatchers.argThat(time -> !time.isEqual(slotBloqueado)), any()))
                .thenReturn(Collections.emptyList());


        // 3. Agendamento: Vamos simular que 08:30 já tem paciente
        LocalDateTime slotAgendado = LocalDateTime.of(data, LocalTime.of(8, 30));

        Mockito.when(agendamentoRepository.existsByMedicoIdAndDataConsulta(eq(medicoId), eq(slotAgendado)))
                .thenReturn(true); // Sim, tem paciente

        // Para os outros horários, retorna false (sem paciente)
        Mockito.when(agendamentoRepository.existsByMedicoIdAndDataConsulta(eq(medicoId),
                        org.mockito.ArgumentMatchers.argThat(time -> !time.isEqual(slotAgendado))))
                .thenReturn(false);

        // --- AÇÃO ---
        List<LocalDateTime> horariosLivres = agendaService.listarHorariosDisponiveis(medicoId, data);

        // --- VERIFICAÇÃO ---
        // Esperamos:
        // 08:00 -> LIVRE ✅
        // 08:15 -> Ocupado por Bloqueio ❌
        // 08:30 -> Ocupado por Agendamento ❌
        // 08:45 -> LIVRE ✅

        Assertions.assertEquals(2, horariosLivres.size()); // Só sobraram 2
        Assertions.assertTrue(horariosLivres.contains(LocalDateTime.of(data, LocalTime.of(8, 0))));
        Assertions.assertTrue(horariosLivres.contains(LocalDateTime.of(data, LocalTime.of(8, 45))));
    }

    @Test
    @DisplayName("✅ Deve salvar configuração corretamente")
    void deveSalvarConfiguracao() {
        ConfiguracaoAgenda config = new ConfiguracaoAgenda();
        Mockito.when(configRepository.save(config)).thenReturn(config);

        ConfiguracaoAgenda salva = agendaService.salvarConfiguracao(config);

        Assertions.assertNotNull(salva);
        Mockito.verify(configRepository, Mockito.times(1)).save(config);
    }
}