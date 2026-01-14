package com.clinic.api.agendamento;

import com.clinic.api.medico.Especialidade;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @InjectMocks
    private AgendamentoService agendamentoService;

    @Mock
    private AgendamentoRepository repository;
    @Mock
    private MedicoRepository medicoRepository;
    @Mock
    private PacienteRepository pacienteRepository;

    @Test
    @DisplayName("❌ Deve falhar ao tentar agendar para uma data no PASSADO")
    void naoDeveAgendarNoPassado() {
        Agendamento agendamento = criarAgendamentoBase();
        agendamento.setDataConsulta(LocalDateTime.now().minusDays(1));

        Mockito.when(medicoRepository.findById(any())).thenReturn(Optional.of(new Medico()));
        Mockito.when(pacienteRepository.findById(any())).thenReturn(Optional.of(new Paciente()));

        assertThrows(RuntimeException.class, () -> agendamentoService.agendar(agendamento));
    }

    @Test
    @DisplayName("❌ Deve falhar se o médico já estiver ocupado")
    void naoDeveAgendarSeMedicoOcupado() {
        Agendamento agendamento = criarAgendamentoBase();
        agendamento.setDataConsulta(LocalDateTime.now().plusDays(1));

        Mockito.when(medicoRepository.findById(any())).thenReturn(Optional.of(new Medico()));
        Mockito.when(pacienteRepository.findById(any())).thenReturn(Optional.of(new Paciente()));
        Mockito.when(repository.existsByMedicoIdAndDataConsulta(any(), any())).thenReturn(true);

        RuntimeException erro = assertThrows(RuntimeException.class, () -> agendamentoService.agendar(agendamento));
        assertEquals("Horário indisponível para este médico.", erro.getMessage());
    }

    @Test
    @DisplayName("❌ Deve falhar se o paciente já tiver agendamento ativo na mesma especialidade")
    void naoDeveAgendarDuplicidadeEspecialidade() {
        Agendamento agendamento = criarAgendamentoBase();
        agendamento.setDataConsulta(LocalDateTime.now().plusDays(1));

        Mockito.when(medicoRepository.findById(any())).thenReturn(Optional.of(new Medico()));
        Mockito.when(pacienteRepository.findById(any())).thenReturn(Optional.of(new Paciente()));

        // CORREÇÃO: Usando o novo método do repositório
        Mockito.when(repository.existsByPacienteIdAndMedico_EspecialidadeAndStatusIn(any(), any(), any()))
                .thenReturn(true);

        assertThrows(RuntimeException.class, () -> agendamentoService.agendar(agendamento));
    }

    @Test
    @DisplayName("✅ Deve agendar via CONVÊNIO (Status AGENDADO direto)")
    void deveAgendarConvenioComSucesso() {
        Medico medico = new Medico();
        medico.setId(UUID.randomUUID());
        // CORREÇÃO: Usando o Enum em vez de String
        medico.setEspecialidade(Especialidade.PEDIATRIA);
        medico.setValorConsulta(new BigDecimal("200.00"));

        Paciente paciente = new Paciente();
        paciente.setId(UUID.randomUUID());
        paciente.setPlano(new Plano());
        paciente.setAtendimentoParticular(false);

        Agendamento agendamento = new Agendamento();
        agendamento.setMedico(medico);
        agendamento.setPaciente(paciente);
        agendamento.setDataConsulta(LocalDateTime.now().plusDays(1));

        Mockito.when(medicoRepository.findById(any())).thenReturn(Optional.of(medico));
        Mockito.when(pacienteRepository.findById(any())).thenReturn(Optional.of(paciente));
        Mockito.when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Agendamento salvo = agendamentoService.agendar(agendamento);

        assertEquals("AGENDADO", salvo.getStatus());
        assertEquals("CONVENIO_APROVADO", salvo.getStatusPagamento());
        assertEquals(BigDecimal.ZERO, salvo.getValorConsulta());
    }

    @Test
    @DisplayName("✅ Deve confirmar agendamento particular (PAGO)")
    void deveConfirmarAgendamentoParticular() {
        UUID id = UUID.randomUUID();
        Agendamento agendamento = new Agendamento();
        agendamento.setStatus("EM_PROCESSAMENTO");
        agendamento.setStatusPagamento("AGUARDANDO_PAGAMENTO");
        agendamento.setDataConsulta(LocalDateTime.now().plusDays(3));

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(agendamento));
        Mockito.when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        agendamentoService.confirmarAgendamento(id);

        assertEquals("AGENDADO", agendamento.getStatus());
        assertEquals("PAGO", agendamento.getStatusPagamento());
    }

    @Test
    @DisplayName("❌ Deve barrar BOLETO com menos de 48h de antecedência")
    void deveBarrarBoletoEmCimaDaHora() {
        UUID id = UUID.randomUUID();
        Agendamento agendamento = new Agendamento();
        agendamento.setStatus("EM_PROCESSAMENTO");
        agendamento.setFormaPagamento("BOLETO");
        agendamento.setDataConsulta(LocalDateTime.now().plusHours(24)); // Apenas 24h

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(agendamento));

        RuntimeException erro = assertThrows(RuntimeException.class, () -> agendamentoService.confirmarAgendamento(id));
        assertTrue(erro.getMessage().contains("Pagamento via boleto exige 48h"));
    }

    // Helper para reduzir repetição de código
    private Agendamento criarAgendamentoBase() {
        Agendamento ag = new Agendamento();
        ag.setMedico(new Medico());
        ag.getMedico().setId(UUID.randomUUID());
        ag.setPaciente(new Paciente());
        ag.getPaciente().setId(UUID.randomUUID());
        return ag;
    }
}