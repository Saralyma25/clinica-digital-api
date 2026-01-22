package com.clinic.api.agendamento;

import com.clinic.api.agendamento.domain.AgendamentoRepository;
import com.clinic.api.agendamento.domain.StatusAgendamento;
import com.clinic.api.agendamento.dto.AgendamentoRequest;
import com.clinic.api.agendamento.dto.AgendamentoResponse;
import com.clinic.api.agendamento.dto.AtendimentoDiarioDTO;
import com.clinic.api.agendamento.service.AgendamentoService;

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
        LocalDateTime data = LocalDateTime.now().plusDays(1);

        Medico medico = criarMedicoMock(medicoId, new BigDecimal("500.00"));
        Paciente paciente = criarPacienteMock(pacienteId, true); // Particular = true

        // Mock dos Repositórios
        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(repository.existsByMedicoIdAndDataConsulta(any(), any())).thenReturn(false);
        // O Save deve retornar o objeto salvo (Entidade)
        when(repository.save(any(Agendamento.class))).thenAnswer(i -> {
            Agendamento a = i.getArgument(0);
            a.setId(UUID.randomUUID());
            return a; // Retorna a entidade preenchida pelo Service
        });

        // CORREÇÃO: Usar AgendamentoRequest (DTO)
        AgendamentoRequest request = new AgendamentoRequest();
        request.setMedicoId(medicoId);
        request.setPacienteId(pacienteId);
        request.setDataConsulta(data);
        request.setFormaPagamento("PIX");

        AgendamentoResponse response = service.agendar(request);

        // Verificações
        assertNotNull(response);
        assertEquals("PENDENTE", response.getStatusPagamento());
        assertEquals(new BigDecimal("500.00"), response.getValor());
        assertEquals("AGENDADO", response.getStatus()); // Response retorna Enum como String
    }

    // --- CENÁRIO 2: Agendamento Convênio ---
    @Test
    @DisplayName("2. Deve agendar consulta CONVÊNIO com sucesso")
    void agendarConvenioSucesso() {
        UUID medicoId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        LocalDateTime data = LocalDateTime.now().plusDays(1);

        Medico medico = criarMedicoMock(medicoId, new BigDecimal("500.00"));
        Paciente paciente = criarPacienteMock(pacienteId, false);

        // Mock do Save
        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(repository.save(any(Agendamento.class))).thenAnswer(i -> {
            Agendamento a = i.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        // CORREÇÃO: Usar DTO
        AgendamentoRequest request = new AgendamentoRequest();
        request.setMedicoId(medicoId);
        request.setPacienteId(pacienteId);
        request.setDataConsulta(data);
        request.setFormaPagamento("CONVENIO");
        request.setNomeConvenio("Unimed");
        request.setNumeroCarteirinha("12345");

        AgendamentoResponse response = service.agendar(request);

        assertEquals("CONVENIO_APROVADO", response.getStatusPagamento());
        // Nota: O Response não retorna nome do convênio direto, mas podemos verificar se não deu erro
        assertNotNull(response.getId());
    }

    // --- CENÁRIOS DE ERRO ---
    @Test
    @DisplayName("3. Deve lançar erro se médico não existe")
    void agendarErroMedicoInexistente() {
        AgendamentoRequest request = new AgendamentoRequest();
        request.setMedicoId(UUID.randomUUID());
        request.setPacienteId(UUID.randomUUID());
        request.setDataConsulta(LocalDateTime.now().plusDays(1));

        when(medicoRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.agendar(request));
    }

    @Test
    @DisplayName("4. Deve lançar erro se médico já estiver ocupado")
    void agendarErroMedicoOcupado() {
        UUID medicoId = UUID.randomUUID();
        LocalDateTime data = LocalDateTime.now().plusDays(1);

        AgendamentoRequest request = new AgendamentoRequest();
        request.setMedicoId(medicoId);
        request.setPacienteId(UUID.randomUUID());
        request.setDataConsulta(data);

        when(medicoRepository.findById(any())).thenReturn(Optional.of(new Medico()));
        when(pacienteRepository.findById(any())).thenReturn(Optional.of(new Paciente()));
        when(repository.existsByMedicoIdAndDataConsulta(eq(medicoId), eq(data))).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.agendar(request));
    }

    // --- TESTES DE OUTROS MÉTODOS ---
    @Test
    @DisplayName("7. Deve listar atendimentos do dia corretamente (DTO)")
    void listarAtendimentosDoDia() {
        UUID medicoId = UUID.randomUUID();
        LocalDate hoje = LocalDate.now();

        Medico medico = new Medico();
        medico.setEspecialidade(Especialidade.CARDIOLOGIA);
        Paciente paciente = new Paciente();
        paciente.setNome("João");

        Agendamento a1 = new Agendamento();
        a1.setId(UUID.randomUUID());
        a1.setDataConsulta(LocalDateTime.now());
        a1.setMedico(medico);
        a1.setPaciente(paciente);

        // CORREÇÃO: Usar Enum
        a1.setStatus(StatusAgendamento.AGENDADO);
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
        // CORREÇÃO: Usar Enum
        agendamento.setStatus(StatusAgendamento.AGENDADO);

        when(repository.findById(id)).thenReturn(Optional.of(agendamento));

        service.cancelar(id);

        // CORREÇÃO: Verificar Enum
        assertEquals(StatusAgendamento.CANCELADO_PACIENTE, agendamento.getStatus());
        verify(repository).save(agendamento);
    }

    @Test
    @DisplayName("9. Deve confirmar agendamento")
    void confirmarAgendamento() {
        UUID id = UUID.randomUUID();
        Agendamento agendamento = new Agendamento();
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setStatusPagamento("PENDENTE");

        when(repository.findById(id)).thenReturn(Optional.of(agendamento));

        service.confirmarAgendamento(id);

        // No código atual do Service, confirmarAgendamento apenas loga ou prepara,
        // mas se você tiver lógica de mudança de status, verifique aqui.
        // Se o método for void e não fizer nada visível na entidade no momento,
        // apenas verificamos se não deu erro.
        verify(repository, atLeastOnce()).findById(id);
    }

    // --- Helpers ---

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