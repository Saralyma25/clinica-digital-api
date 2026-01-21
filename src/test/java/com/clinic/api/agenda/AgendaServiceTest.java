package com.clinic.api.agenda.service;

import com.clinic.api.agenda.domain.*;
import com.clinic.api.agenda.dto.AgendaConfigRequest;
import com.clinic.api.agenda.dto.BloqueioRequest;
import com.clinic.api.agendamento.AgendamentoRepository;
import com.clinic.api.medico.Medico;
import com.clinic.api.medico.domain.MedicoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

    @Mock
    private ConfiguracaoAgendaRepository configRepository;
    @Mock
    private BloqueioAgendaRepository bloqueioRepository;
    @Mock
    private AgendamentoRepository agendamentoRepository;
    @Mock
    private MedicoRepository medicoRepository;

    @InjectMocks
    private AgendaService service;

    // --- TESTES DE CONFIGURAÇÃO (salvarConfiguracao) ---

    @Test
    @DisplayName("1. Deve criar nova configuração de agenda quando não existir prévia")
    void salvarConfiguracao_CriaNova() {
        UUID medicoId = UUID.randomUUID();
        AgendaConfigRequest request = new AgendaConfigRequest(medicoId, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0));
        Medico medico = new Medico();
        medico.setId(medicoId);

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
        when(configRepository.findByMedicoIdAndDiaSemana(medicoId, DayOfWeek.MONDAY)).thenReturn(Optional.empty());
        when(configRepository.save(any(ConfiguracaoAgenda.class))).thenAnswer(i -> i.getArgument(0));

        ConfiguracaoAgenda resultado = service.salvarConfiguracao(request);

        assertNotNull(resultado);
        assertEquals(DayOfWeek.MONDAY, resultado.getDiaSemana());
        verify(configRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("2. Deve atualizar configuração existente de agenda")
    void salvarConfiguracao_AtualizaExistente() {
        UUID medicoId = UUID.randomUUID();
        AgendaConfigRequest request = new AgendaConfigRequest(medicoId, DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(12, 0));

        ConfiguracaoAgenda configAntiga = new ConfiguracaoAgenda();
        configAntiga.setHorarioInicio(LocalTime.of(8, 0));

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(new Medico()));
        when(configRepository.findByMedicoIdAndDiaSemana(medicoId, DayOfWeek.FRIDAY)).thenReturn(Optional.of(configAntiga));
        when(configRepository.save(any(ConfiguracaoAgenda.class))).thenAnswer(i -> i.getArgument(0));

        ConfiguracaoAgenda resultado = service.salvarConfiguracao(request);

        assertEquals(LocalTime.of(9, 0), resultado.getHorarioInicio());
        verify(configRepository, times(1)).save(configAntiga);
    }

    @Test
    @DisplayName("3. Deve lançar exceção ao configurar agenda de médico inexistente")
    void salvarConfiguracao_ErroMedicoNaoEncontrado() {
        UUID medicoId = UUID.randomUUID();
        AgendaConfigRequest request = new AgendaConfigRequest(medicoId, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0));

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.salvarConfiguracao(request));
        verify(configRepository, never()).save(any());
    }

    // --- TESTES DE BLOQUEIO (criarBloqueio) ---

    @Test
    @DisplayName("4. Deve criar bloqueio com sucesso")
    void criarBloqueio_Sucesso() {
        UUID medicoId = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fim = inicio.plusHours(1);
        BloqueioRequest request = new BloqueioRequest(medicoId, inicio, fim, "Almoço");

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(new Medico()));
        when(bloqueioRepository.save(any(BloqueioAgenda.class))).thenAnswer(i -> i.getArgument(0));

        BloqueioAgenda bloqueio = service.criarBloqueio(request);

        assertNotNull(bloqueio);
        assertEquals("Almoço", bloqueio.getMotivo());
    }

    @Test
    @DisplayName("5. Deve impedir bloqueio onde data fim é anterior ao início")
    void criarBloqueio_ErroDatasInvalidas() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(2);
        LocalDateTime fim = LocalDateTime.now().plusDays(1);
        BloqueioRequest request = new BloqueioRequest(UUID.randomUUID(), inicio, fim, "Erro");

        assertThrows(RuntimeException.class, () -> service.criarBloqueio(request));
        verify(bloqueioRepository, never()).save(any());
    }

    @Test
    @DisplayName("6. Deve lançar exceção ao bloquear agenda de médico inexistente")
    void criarBloqueio_ErroMedicoNaoEncontrado() {
        UUID medicoId = UUID.randomUUID();
        BloqueioRequest request = new BloqueioRequest(medicoId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1), "Férias");

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.criarBloqueio(request));
    }

    // --- TESTES DE DISPONIBILIDADE (listarHorariosDisponiveis) ---

    @Test
    @DisplayName("7. Deve retornar lista vazia se a data solicitada for no passado")
    void listarHorarios_DataPassada() {
        List<LocalDateTime> resultado = service.listarHorariosDisponiveis(UUID.randomUUID(), LocalDate.now().minusDays(1));
        assertTrue(resultado.isEmpty());
        verifyNoInteractions(configRepository);
    }

    @Test
    @DisplayName("8. Deve retornar lista vazia se o médico não tiver configuração para o dia")
    void listarHorarios_SemConfiguracao() {
        UUID medicoId = UUID.randomUUID();
        LocalDate amanha = LocalDate.now().plusDays(1);

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(new Medico()));
        when(configRepository.findByMedicoIdAndDiaSemana(medicoId, amanha.getDayOfWeek())).thenReturn(Optional.empty());

        List<LocalDateTime> resultado = service.listarHorariosDisponiveis(medicoId, amanha);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("9. Deve retornar lista vazia se a configuração do dia estiver inativa")
    void listarHorarios_ConfiguracaoInativa() {
        UUID medicoId = UUID.randomUUID();
        LocalDate amanha = LocalDate.now().plusDays(1);
        ConfiguracaoAgenda config = new ConfiguracaoAgenda();
        config.setAtivo(false);

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(new Medico()));
        when(configRepository.findByMedicoIdAndDiaSemana(medicoId, amanha.getDayOfWeek())).thenReturn(Optional.of(config));

        List<LocalDateTime> resultado = service.listarHorariosDisponiveis(medicoId, amanha);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("10. Deve listar horários corretamente (Cenário Perfeito)")
    void listarHorarios_SucessoTotal() {
        UUID medicoId = UUID.randomUUID();
        LocalDate amanha = LocalDate.now().plusDays(1);

        Medico medico = new Medico();
        medico.setDuracaoConsulta(60);

        ConfiguracaoAgenda config = new ConfiguracaoAgenda();
        config.setHorarioInicio(LocalTime.of(8, 0));
        config.setHorarioFim(LocalTime.of(10, 0));
        config.setAtivo(true);

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
        when(configRepository.findByMedicoIdAndDiaSemana(medicoId, amanha.getDayOfWeek())).thenReturn(Optional.of(config));

        // Mocks flexíveis
        when(bloqueioRepository.findBloqueiosNoIntervalo(any(), any(), any())).thenReturn(Collections.emptyList());
        when(agendamentoRepository.existsByMedicoIdAndDataConsulta(any(), any())).thenReturn(false);

        List<LocalDateTime> slots = service.listarHorariosDisponiveis(medicoId, amanha);

        assertEquals(2, slots.size());
        assertTrue(slots.contains(amanha.atTime(8, 0)));
        assertTrue(slots.contains(amanha.atTime(9, 0)));
    }

    @Test
    @DisplayName("11. Deve filtrar horários que possuem bloqueio ou agendamento")
    void listarHorarios_ComFiltrosDeOcupacao() {
        /*
         CENÁRIO:
         - Horário: 08h às 11h (Slots: 08, 09, 10)
         - 08:00 -> Livre
         - 09:00 -> Tem Bloqueio
         - 10:00 -> Tem Agendamento
         - Resultado esperado: Apenas 08:00 livre
        */

        UUID medicoId = UUID.randomUUID();
        LocalDate amanha = LocalDate.now().plusDays(1);

        Medico medico = new Medico();
        medico.setDuracaoConsulta(60);

        ConfiguracaoAgenda config = new ConfiguracaoAgenda();
        config.setHorarioInicio(LocalTime.of(8, 0));
        config.setHorarioFim(LocalTime.of(11, 0));
        config.setAtivo(true);

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
        when(configRepository.findByMedicoIdAndDiaSemana(medicoId, amanha.getDayOfWeek())).thenReturn(Optional.of(config));

        // --- CORREÇÃO DO MOCKITO STRICT ---
        // Usamos 'any()' para as datas e respondemos dinamicamente baseado no horário consultado

        // 1. Mock de Bloqueios
        when(bloqueioRepository.findBloqueiosNoIntervalo(eq(medicoId), any(), any()))
                .thenAnswer(invocation -> {
                    LocalDateTime inicio = invocation.getArgument(1);
                    // Se for o horário das 09:00, retorna um bloqueio
                    if (inicio.getHour() == 9) {
                        return List.of(new BloqueioAgenda());
                    }
                    return Collections.emptyList(); // Nos outros horários, retorna vazio
                });

        // 2. Mock de Agendamentos
        when(agendamentoRepository.existsByMedicoIdAndDataConsulta(eq(medicoId), any()))
                .thenAnswer(invocation -> {
                    LocalDateTime data = invocation.getArgument(1);
                    // Se for o horário das 10:00, retorna true (ocupado)
                    return data.getHour() == 10;
                });

        List<LocalDateTime> slots = service.listarHorariosDisponiveis(medicoId, amanha);



        assertEquals(1, slots.size()); // Só sobrou 1
        assertEquals(amanha.atTime(8, 0), slots.get(0)); // E deve ser às 08:00
    }
}