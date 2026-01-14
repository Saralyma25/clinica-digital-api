package com.clinic.api.prontuario;

import com.clinic.api.agendamento.Agendamento;
import com.clinic.api.agendamento.AgendamentoRepository;
import com.clinic.api.medico.Medico;
import com.clinic.api.paciente.Paciente;
import com.clinic.api.paciente.PacienteRepository;
import com.clinic.api.prontuario.dto.FolhaDeRostoDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProntuarioServiceTest {

    @InjectMocks
    private ProntuarioService service;

    @Mock
    private ProntuarioRepository repository;
    @Mock
    private AgendamentoRepository agendamentoRepository;
    @Mock
    private DadosClinicosFixosRepository dadosFixosRepository;
    @Mock
    private PacienteRepository pacienteRepository;
    @Mock
    private DeepSeekService deepSeekService;

    @Test
    void deveSalvarProntuario_QuandoMedicoForDonoDoAgendamento() {
        // CENÁRIO
        UUID medicoId = UUID.randomUUID();
        UUID agendamentoId = UUID.randomUUID();

        Medico medico = new Medico();
        medico.setId(medicoId);

        Agendamento agendamento = new Agendamento();
        agendamento.setId(agendamentoId);
        agendamento.setMedico(medico);

        Prontuario prontuario = new Prontuario();
        prontuario.setAgendamento(agendamento);

        // Mocks
        when(agendamentoRepository.existsById(agendamentoId)).thenReturn(true);
        when(repository.findByAgendamentoId(agendamentoId)).thenReturn(Optional.empty());
        when(repository.save(any(Prontuario.class))).thenReturn(prontuario);

        // AÇÃO (Aqui estava o erro: agora passamos o medicoId)
        Prontuario salvo = service.salvar(prontuario, medicoId);

        // VERIFICAÇÃO
        Assertions.assertNotNull(salvo);
        verify(repository, times(1)).save(prontuario);
    }

    @Test
    void deveBloquearSalvar_QuandoMedicoNaoForDono() {
        // CENÁRIO
        UUID medicoDonoId = UUID.randomUUID();
        UUID medicoIntrusoId = UUID.randomUUID();

        Medico medico = new Medico();
        medico.setId(medicoDonoId); // O dono do agendamento é X

        Agendamento agendamento = new Agendamento();
        agendamento.setMedico(medico);

        Prontuario prontuario = new Prontuario();
        prontuario.setAgendamento(agendamento);

        // AÇÃO & VERIFICAÇÃO
        RuntimeException erro = Assertions.assertThrows(RuntimeException.class, () -> {
            service.salvar(prontuario, medicoIntrusoId); // Quem tenta salvar é Y
        });

        Assertions.assertEquals("Segurança: Você só pode registrar ou editar prontuários de seus próprios atendimentos.", erro.getMessage());
    }

    @Test
    void deveGerarFolhaDeRosto_Corretamente() {
        // CENÁRIO
        UUID pacienteId = UUID.randomUUID();
        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);
        paciente.setNome("Sara Teste");
        paciente.setDataNascimento(LocalDate.of(1990, 1, 1));

        // Mocks
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(dadosFixosRepository.findById(pacienteId)).thenReturn(Optional.empty());
        when(repository.buscarHistoricoCompletoDoPaciente(pacienteId)).thenReturn(Collections.emptyList());
        when(deepSeekService.gerarResumoClinico(anyString())).thenReturn("Resumo Mock IA");

        // AÇÃO
        FolhaDeRostoDTO dto = service.obterFolhaDeRosto(pacienteId);

        // VERIFICAÇÃO
        Assertions.assertNotNull(dto);
        Assertions.assertEquals("Sara Teste", dto.getNome());
    }
}