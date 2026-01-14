package com.clinic.api.agendamento;

import com.clinic.api.agendamento.dto.AtendimentoDiarioDTO;
import com.clinic.api.medico.Especialidade;
import com.clinic.api.medico.Medico;
import com.clinic.api.medico.MedicoRepository;
import com.clinic.api.paciente.Paciente;
import com.clinic.api.paciente.PacienteRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @InjectMocks
    private AgendamentoService service; // Aqui sim usamos "service"

    @Mock
    private AgendamentoRepository repository; // Aqui sim usamos "repository"
    @Mock
    private MedicoRepository medicoRepository;
    @Mock
    private PacienteRepository pacienteRepository;

    @Test
    void deveListarAtendimentosDoDia_Corretamente() {
        // CENÁRIO
        UUID medicoId = UUID.randomUUID();
        LocalDate dataHoje = LocalDate.now();

        Medico medico = new Medico();
        medico.setId(medicoId);
        medico.setEspecialidade(Especialidade.CARDIOLOGIA);

        Paciente paciente = new Paciente();
        paciente.setNome("Paciente Teste");
        paciente.setAtendimentoParticular(true);

        Agendamento agendamento = new Agendamento();
        agendamento.setId(UUID.randomUUID());
        agendamento.setDataConsulta(LocalDateTime.now());
        agendamento.setMedico(medico);
        agendamento.setPaciente(paciente);
        agendamento.setStatus("AGENDADO");
        agendamento.setStatusPagamento("PENDENTE");

        // Mock do Repository
        when(repository.findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(any(), any(), any()))
                .thenReturn(List.of(agendamento));

        // AÇÃO
        List<AtendimentoDiarioDTO> lista = service.listarAtendimentosDoDia(medicoId, dataHoje);

        // VERIFICAÇÃO
        Assertions.assertFalse(lista.isEmpty());
        Assertions.assertEquals("PENDENTE", lista.get(0).getStatusPagamento());
        Assertions.assertEquals("CARDIOLOGIA", lista.get(0).getEspecialidade());
    }
}