package com.clinic.api.agendamento;

import com.clinic.api.medico.Medico;
import com.clinic.api.medico.MedicoRepository;
import com.clinic.api.paciente.Paciente;
import com.clinic.api.paciente.PacienteRepository;
import com.clinic.api.plano.Plano;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class) // Habilita o Mockito
class AgendamentoServiceTest {

    @InjectMocks // Quem estamos testando (O Service Real)
    private AgendamentoService agendamentoService;

    @Mock // Quem vamos "fingir" (Os Repositórios)
    private AgendamentoRepository repository;
    @Mock
    private MedicoRepository medicoRepository;
    @Mock
    private PacienteRepository pacienteRepository;

    @Test
    @DisplayName("❌ Deve falhar ao tentar agendar para uma data no PASSADO")
    void naoDeveAgendarNoPassado() {
        // Cenario
        Agendamento agendamento = new Agendamento();
        agendamento.setMedico(new Medico());
        agendamento.getMedico().setId(UUID.randomUUID());
        agendamento.setPaciente(new Paciente());
        agendamento.getPaciente().setId(UUID.randomUUID());

        // Define data para ONTEM
        agendamento.setDataConsulta(LocalDateTime.now().minusDays(1));

        // Mockando as buscas de ID para não falhar antes da hora
        Mockito.when(medicoRepository.findById(any())).thenReturn(Optional.of(new Medico()));
        Mockito.when(pacienteRepository.findById(any())).thenReturn(Optional.of(new Paciente()));

        // Ação e Verificação (Espero que estoure um erro RuntimeException)
        assertThrows(RuntimeException.class, () -> {
            agendamentoService.agendar(agendamento);
        });
    }

    @Test
    @DisplayName("❌ Deve falhar se o médico já tiver agendamento no horário")
    void naoDeveAgendarSeMedicoOcupado() {
        // Cenario
        Agendamento agendamento = new Agendamento();
        agendamento.setMedico(new Medico());
        agendamento.getMedico().setId(UUID.randomUUID()); // ID Falso
        agendamento.setPaciente(new Paciente());
        agendamento.getPaciente().setId(UUID.randomUUID());

        // Data futura válida
        agendamento.setDataConsulta(LocalDateTime.now().plusDays(1));

        // Mocks
        Mockito.when(medicoRepository.findById(any())).thenReturn(Optional.of(new Medico()));
        Mockito.when(pacienteRepository.findById(any())).thenReturn(Optional.of(new Paciente()));

        // O PULO DO GATO: Fingimos que o banco disse "SIM, JÁ EXISTE AGENDAMENTO"
        Mockito.when(repository.existsByMedicoIdAndDataConsulta(any(), any())).thenReturn(true);

        // Ação e Verificação
        RuntimeException erro = assertThrows(RuntimeException.class, () -> {
            agendamentoService.agendar(agendamento);
        });

        Assertions.assertEquals("Horário indisponível (Já reservado).", erro.getMessage());
    }

    @Test
    @DisplayName("✅ Deve agendar com sucesso se tudo estiver correto")
    void deveAgendarComSucesso() {
        // Cenario
        UUID medicoId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();

        Medico medico = new Medico();
        medico.setId(medicoId);
        medico.setValorConsulta(new java.math.BigDecimal("200.00"));
        medico.setEspecialidade("Cardiologia");

        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);
        paciente.setAtendimentoParticular(true); // Vai pagar particular

        Agendamento agendamento = new Agendamento();
        agendamento.setMedico(medico);
        agendamento.setPaciente(paciente);
        agendamento.setDataConsulta(LocalDateTime.now().plusDays(1)); // Futuro

        // Mocks (Comportamento Feliz)
        Mockito.when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
        Mockito.when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        Mockito.when(repository.existsByMedicoIdAndDataConsulta(any(), any())).thenReturn(false); // Médico Livre
        // Mock da Trava de Especialidade
        Mockito.when(repository.existsByPacienteIdAndMedico_EspecialidadeAndStatusNot(any(), any(), any())).thenReturn(false);

        // Quando salvar, retorna o próprio objeto
        Mockito.when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Ação
        Agendamento agendamentoSalvo = agendamentoService.agendar(agendamento);

        // Verificação
        Assertions.assertNotNull(agendamentoSalvo);
        Assertions.assertEquals("EM_PROCESSAMENTO", agendamentoSalvo.getStatus());
        Assertions.assertEquals(medico.getValorConsulta(), agendamentoSalvo.getValorConsulta());
    }
}