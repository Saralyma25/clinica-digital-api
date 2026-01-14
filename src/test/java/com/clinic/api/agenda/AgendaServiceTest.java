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
        UUID medicoId = UUID.randomUUID();
        LocalDate segundaFeira = LocalDate.of(2025, 1, 13);

        // NOVO: Mock para validar que o médico existe
        Mockito.when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(new Medico()));

        Mockito.when(configRepository.findByMedicoIdAndDiaSemana(eq(medicoId), eq(DayOfWeek.MONDAY)))
                .thenReturn(Optional.empty());

        List<LocalDateTime> horarios = agendaService.listarHorariosDisponiveis(medicoId, segundaFeira);

        Assertions.assertTrue(horarios.isEmpty());
    }

    @Test
    @DisplayName("✅ Deve gerar slots corretamente descontando AGENDAMENTOS e BLOQUEIOS")
    void deveGerarGradeDescontandoOcupacoes() {
        UUID medicoId = UUID.randomUUID();
        LocalDate data = LocalDate.of(2025, 1, 13);

        // NOVO: Mock para validar que o médico existe
        Mockito.when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(new Medico()));

        ConfiguracaoAgenda config = new ConfiguracaoAgenda();
        config.setAtivo(true);
        config.setHorarioInicio(LocalTime.of(8, 0));
        config.setHorarioFim(LocalTime.of(9, 0));

        Mockito.when(configRepository.findByMedicoIdAndDiaSemana(eq(medicoId), eq(DayOfWeek.MONDAY)))
                .thenReturn(Optional.of(config));

        LocalDateTime slotBloqueado = LocalDateTime.of(data, LocalTime.of(8, 15));
        Mockito.when(bloqueioRepository.findBloqueiosNoIntervalo(eq(medicoId), eq(slotBloqueado), any()))
                .thenReturn(List.of(new BloqueioAgenda()));

        Mockito.when(bloqueioRepository.findBloqueiosNoIntervalo(eq(medicoId),
                        org.mockito.ArgumentMatchers.argThat(time -> !time.isEqual(slotBloqueado)), any()))
                .thenReturn(Collections.emptyList());

        LocalDateTime slotAgendado = LocalDateTime.of(data, LocalTime.of(8, 30));
        Mockito.when(agendamentoRepository.existsByMedicoIdAndDataConsulta(eq(medicoId), eq(slotAgendado)))
                .thenReturn(true);

        Mockito.when(agendamentoRepository.existsByMedicoIdAndDataConsulta(eq(medicoId),
                        org.mockito.ArgumentMatchers.argThat(time -> !time.isEqual(slotAgendado))))
                .thenReturn(false);

        List<LocalDateTime> horariosLivres = agendaService.listarHorariosDisponiveis(medicoId, data);

        Assertions.assertEquals(2, horariosLivres.size());
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