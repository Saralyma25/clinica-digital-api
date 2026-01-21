package com.clinic.api.agendamento.service;

import com.clinic.api.agendamento.Agendamento;
import com.clinic.api.agendamento.AgendamentoRepository;
import com.clinic.api.agendamento.dto.AtendimentoDiarioDTO;
import com.clinic.api.convenio.Convenio;
import com.clinic.api.medico.Medico;
import com.clinic.api.medico.domain.MedicoRepository;
import com.clinic.api.medico.enun.Especialidade;
import com.clinic.api.paciente.Paciente;
import com.clinic.api.paciente.domain.PacienteRepository;
import com.clinic.api.plano.Plano;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock private AgendamentoRepository repository;
    @Mock private MedicoRepository medicoRepository;
    @Mock private PacienteRepository pacienteRepository;

    @InjectMocks private AgendamentoService service;

    // --- CENÁRIO 1: Agendamento Particular ---
    @Test
    @DisplayName("1. Deve agendar consulta PARTICULAR com sucesso (usa valor do médico)")
    void agendarParticularSucesso() {
        UUID medicoId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        Medico medico = criarMedicoMock(medicoId, new BigDecimal("500.00"));
        Paciente paciente = criarPacienteMock(pacienteId, true); // Particular = true
        Agendamento agendamento = criarAgendamentoMock(medicoId, pacienteId);

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(repository.existsByMedicoIdAndDataConsulta(any(), any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Agendamento salvo = service.agendar(agendamento);

        assertEquals("PENDENTE", salvo.getStatusPagamento());
        assertEquals("PARTICULAR", salvo.getNomeConvenio());
        assertEquals(new BigDecimal("500.00"), salvo.getValorConsulta());
        assertEquals("AGENDADO", salvo.getStatus());
    }

    // --- CENÁRIO 2: Agendamento Convênio ---
    @Test
    @DisplayName("2. Deve agendar consulta CONVÊNIO com sucesso (usa valor do plano)")
    void agendarConvenioSucesso() {
        UUID medicoId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        Medico medico = criarMedicoMock(medicoId, new BigDecimal("500.00")); // Valor médico ignora
        Paciente paciente = criarPacienteMock(pacienteId, false); // Particular = false

        // Configura Plano e Convênio Mockados
        Plano plano = new Plano();
        plano.setNome("Ouro");
        plano.setValorRepasse(new BigDecimal("150.00"));
        Convenio convenio = new Convenio();
        convenio.setNome("Unimed");
        plano.setConvenio(convenio);
        paciente.setPlano(plano);

        Agendamento agendamento = criarAgendamentoMock(medicoId, pacienteId);

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Agendamento salvo = service.agendar(agendamento);

        assertEquals("CONVENIO_APROVADO", salvo.getStatusPagamento());
        assertEquals("Unimed - Ouro", salvo.getNomeConvenio());
        assertEquals(new BigDecimal("150.00"), salvo.getValorConsulta());
    }

    // --- CENÁRIOS DE ERRO ---
    @Test
    @DisplayName("3. Deve lançar erro se médico não existe")
    void agendarErroMedicoInexistente() {
        Agendamento agendamento = criarAgendamentoMock(UUID.randomUUID(), UUID.randomUUID());
        when(medicoRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.agendar(agendamento));
    }

    @Test
    @DisplayName("4. Deve lançar erro se data for no passado")
    void agendarErroDataPassada() {
        UUID medicoId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        Agendamento agendamento = criarAgendamentoMock(medicoId, pacienteId);
        agendamento.setDataConsulta(LocalDateTime.now().minusDays(1)); // Ontem

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(new Medico()));
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(new Paciente()));

        assertThrows(RuntimeException.class, () -> service.agendar(agendamento));
    }

    @Test
    @DisplayName("5. Deve lançar erro se médico já estiver ocupado no horário")
    void agendarErroMedicoOcupado() {
        UUID medicoId = UUID.randomUUID();
        Agendamento agendamento = criarAgendamentoMock(medicoId, UUID.randomUUID());

        when(medicoRepository.findById(any())).thenReturn(Optional.of(new Medico()));
        when(pacienteRepository.findById(any())).thenReturn(Optional.of(new Paciente()));
        when(repository.existsByMedicoIdAndDataConsulta(eq(medicoId), any())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.agendar(agendamento));
    }

    @Test
    @DisplayName("6. Deve lançar erro se paciente Convênio não tiver Plano vinculado")
    void agendarErroConvenioSemPlano() {
        UUID pacienteId = UUID.randomUUID();
        Paciente paciente = criarPacienteMock(pacienteId, false);
        paciente.setPlano(null); // Erro aqui

        Agendamento agendamento = criarAgendamentoMock(UUID.randomUUID(), pacienteId);

        when(medicoRepository.findById(any())).thenReturn(Optional.of(new Medico()));
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.agendar(agendamento));
        assertEquals("Paciente marcado como Convênio, mas não possui Plano vinculado.", ex.getMessage());
    }

    // --- TESTES DE OUTROS MÉTODOS ---
    @Test
    @DisplayName("7. Deve listar atendimentos do dia corretamente (DTO)")
    void listarAtendimentosDoDia() {
        UUID medicoId = UUID.randomUUID();
        LocalDate hoje = LocalDate.now();

        Medico medico = new Medico();
        medico.setEspecialidade(Especialidade.CARDIOLOGIA); // Garanta que este ENUM existe
        Paciente paciente = new Paciente();
        paciente.setNome("João");

        Agendamento a1 = new Agendamento();
        a1.setId(UUID.randomUUID());
        a1.setDataConsulta(LocalDateTime.now());
        a1.setMedico(medico);
        a1.setPaciente(paciente);
        a1.setStatus("AGENDADO");
        a1.setStatusPagamento("PAGO");

        when(repository.findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(eq(medicoId), any(), any()))
                .thenReturn(List.of(a1));

        List<AtendimentoDiarioDTO> lista = service.listarAtendimentosDoDia(medicoId, hoje);

        assertEquals(1, lista.size());
        assertEquals("João", lista.get(0).getPacienteNome());
        assertEquals("CARDIOLOGIA", lista.get(0).getEspecialidade());
    }

    @Test
    @DisplayName("8. Deve cancelar agendamento")
    void cancelarAgendamento() {
        UUID id = UUID.randomUUID();
        Agendamento agendamento = new Agendamento();
        agendamento.setStatus("AGENDADO");

        when(repository.findById(id)).thenReturn(Optional.of(agendamento));

        service.cancelar(id);

        assertEquals("CANCELADO", agendamento.getStatus());
        verify(repository).save(agendamento);
    }

    @Test
    @DisplayName("9. Deve confirmar agendamento e mudar pagamento se for particular")
    void confirmarAgendamentoParticular() {
        UUID id = UUID.randomUUID();
        Agendamento agendamento = new Agendamento();
        agendamento.setStatus("AGENDADO");
        agendamento.setStatusPagamento("PENDENTE"); // Particular

        when(repository.findById(id)).thenReturn(Optional.of(agendamento));

        service.confirmarAgendamento(id);

        assertEquals("CONFIRMADO", agendamento.getStatus());

        // --- CORREÇÃO AQUI: getStatusPagamento() ---
        assertEquals("PAGO", agendamento.getStatusPagamento());
    }

    @Test
    @DisplayName("10. Deve limpar rascunhos antigos (Faxineiro)")
    void limparRascunhos() {
        service.limparRascunhos();
        // Verifica se o delete foi chamado com os parâmetros certos
        verify(repository, times(1))
                .deleteByStatusAndDataCadastroBefore(eq("EM_PROCESSAMENTO"), any(LocalDateTime.class));
    }

    // --- Helpers ---
    private Agendamento criarAgendamentoMock(UUID medicoId, UUID pacienteId) {
        Agendamento a = new Agendamento();
        Medico m = new Medico(); m.setId(medicoId);
        Paciente p = new Paciente(); p.setId(pacienteId);
        a.setMedico(m);
        a.setPaciente(p);
        a.setDataConsulta(LocalDateTime.now().plusDays(1));
        return a;
    }

    private Medico criarMedicoMock(UUID id, BigDecimal valor) {
        Medico m = new Medico();
        m.setId(id);
        m.setValorConsulta(valor);
        m.setNome("Dr. Teste");
        return m;
    }

    private Paciente criarPacienteMock(UUID id, boolean particular) {
        Paciente p = new Paciente();
        p.setId(id);
        p.setAtendimentoParticular(particular);
        p.setNome("Paciente Teste");
        return p;
    }
}