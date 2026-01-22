package com.clinic.api.agenda;

import com.clinic.api.agenda.domain.BloqueioAgendaRepository; // Import necessário
import com.clinic.api.agenda.domain.ConfiguracaoAgenda;
import com.clinic.api.agenda.domain.ConfiguracaoAgendaRepository;
import com.clinic.api.agenda.service.AgendaService;
import com.clinic.api.agendamento.domain.AgendamentoRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

    @InjectMocks
    private AgendaService service;

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private ConfiguracaoAgendaRepository configRepository;

    @Mock // ADICIONADO: O Service precisa deste repositório agora
    private BloqueioAgendaRepository bloqueioRepository;

    // Helper para criar configuração padrão (08:00 as 18:00)
    private void mockConfiguracaoPadrao(UUID medicoId) {
        ConfiguracaoAgenda config = new ConfiguracaoAgenda();
        config.setMedicoId(medicoId);
        config.setDiaSemana(DayOfWeek.FRIDAY); // Exemplo
        config.setHoraInicio(LocalTime.of(8, 0)); // Nome correto da entidade
        config.setHoraFim(LocalTime.of(18, 0));   // Nome correto da entidade
        config.setAtivo(true);
        config.setIntervaloMinutos(60); // Consultas de 1h

        // Mock do repositório de Configuração
        lenient().when(configRepository.findByMedicoIdAndDiaSemana(eq(medicoId), any(DayOfWeek.class)))
                .thenReturn(Optional.of(config));

        // Mock do repositório de Bloqueio (Default: Sem bloqueios)
        lenient().when(bloqueioRepository.findBloqueiosNoIntervalo(any(), any(), any()))
                .thenReturn(List.of());
    }





    @Test
    @DisplayName("3. Deve lançar erro se tentar buscar agenda de Médico inexistente")
    void erroMedicoInexistente() {
        UUID idInvalido = UUID.randomUUID();
        LocalDate data = LocalDate.now().plusDays(1);

        // Se o service usa existsById:
        when(medicoRepository.existsById(idInvalido)).thenReturn(false);
        // Se usa findById:
        lenient().when(medicoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.listarHorariosDisponiveis(idInvalido, data));
    }

    @Test
    @DisplayName("4. Deve retornar lista vazia se o dia estiver COMPLETAMENTE lotado")
    void diaCompletamenteLotado() {
        UUID medicoId = UUID.randomUUID();
        LocalDate data = LocalDate.now().plusDays(1);
        Medico medico = new Medico();
        medico.setId(medicoId);

        lenient().when(medicoRepository.existsById(medicoId)).thenReturn(true);
        lenient().when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));

        mockConfiguracaoPadrao(medicoId);

        // Mocka que TUDO retorna true (ocupado)
        when(agendamentoRepository.existsByMedicoIdAndDataConsulta(any(), any())).thenReturn(true);

        List<LocalDateTime> horarios = service.listarHorariosDisponiveis(medicoId, data);

        assertNotNull(horarios);
        assertTrue(horarios.isEmpty(), "A lista deveria estar vazia");
    }

    @Test
    @DisplayName("5. Validação de Limite: O primeiro horário deve ser o de abertura")
    void validarHorarioAbertura() {
        UUID medicoId = UUID.randomUUID();
        LocalDate data = LocalDate.now().plusDays(5);
        Medico medico = new Medico();
        medico.setId(medicoId);

        lenient().when(medicoRepository.existsById(medicoId)).thenReturn(true);
        lenient().when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));

        mockConfiguracaoPadrao(medicoId);

        when(agendamentoRepository.existsByMedicoIdAndDataConsulta(any(), any())).thenReturn(false);

        List<LocalDateTime> horarios = service.listarHorariosDisponiveis(medicoId, data);

        assertFalse(horarios.isEmpty());
        assertEquals(8, horarios.get(0).getHour());
    }

    @Test
    @DisplayName("6. Validação de Limite: Não deve gerar horários após o fechamento")
    void validarHorarioFechamento() {
        UUID medicoId = UUID.randomUUID();
        LocalDate data = LocalDate.now().plusDays(5);
        Medico medico = new Medico();
        medico.setId(medicoId);

        lenient().when(medicoRepository.existsById(medicoId)).thenReturn(true);
        lenient().when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));

        mockConfiguracaoPadrao(medicoId);

        when(agendamentoRepository.existsByMedicoIdAndDataConsulta(any(), any())).thenReturn(false);

        List<LocalDateTime> horarios = service.listarHorariosDisponiveis(medicoId, data);

        assertFalse(horarios.isEmpty());
        LocalDateTime ultimoHorario = horarios.get(horarios.size() - 1);

        assertTrue(ultimoHorario.getHour() < 18);
    }

    @Test
    @DisplayName("7. Deve garantir que a lista está ordenada cronologicamente")
    void validarOrdenacaoCronologica() {
        UUID medicoId = UUID.randomUUID();
        LocalDate data = LocalDate.now().plusDays(2);
        Medico medico = new Medico();
        medico.setId(medicoId);

        lenient().when(medicoRepository.existsById(medicoId)).thenReturn(true);
        lenient().when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));

        mockConfiguracaoPadrao(medicoId);

        when(agendamentoRepository.existsByMedicoIdAndDataConsulta(any(), any())).thenReturn(false);

        List<LocalDateTime> horarios = service.listarHorariosDisponiveis(medicoId, data);

        if (horarios.size() > 1) {
            assertTrue(horarios.get(1).isAfter(horarios.get(0)));
        }
    }

    @Test
    @DisplayName("8. Não deve retornar horários passados se a busca for para o dia de HOJE")
    void filtrarHorariosPassadosNoDiaDeHoje() {
        UUID medicoId = UUID.randomUUID();
        LocalDate hoje = LocalDate.now();
        Medico medico = new Medico();
        medico.setId(medicoId);

        lenient().when(medicoRepository.existsById(medicoId)).thenReturn(true);
        lenient().when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));

        mockConfiguracaoPadrao(medicoId);

        lenient().when(agendamentoRepository.existsByMedicoIdAndDataConsulta(any(), any())).thenReturn(false);

        List<LocalDateTime> horarios = service.listarHorariosDisponiveis(medicoId, hoje);

        LocalDateTime agora = LocalDateTime.now();
        boolean temHorarioPassado = horarios.stream().anyMatch(h -> h.isBefore(agora));
        assertFalse(temHorarioPassado);
    }

    @Test
    @DisplayName("9. Deve retornar lista vazia se médico não tiver configuração para o dia")
    void erroSemConfiguracao() {
        UUID medicoId = UUID.randomUUID();
        LocalDate data = LocalDate.now().plusDays(1);

        lenient().when(medicoRepository.existsById(medicoId)).thenReturn(true);

        // Retorna Empty (Médico não trabalha neste dia)
        when(configRepository.findByMedicoIdAndDiaSemana(any(), any())).thenReturn(Optional.empty());

        List<LocalDateTime> horarios = service.listarHorariosDisponiveis(medicoId, data);

        assertTrue(horarios.isEmpty(), "Sem config = Sem horários");
    }
}