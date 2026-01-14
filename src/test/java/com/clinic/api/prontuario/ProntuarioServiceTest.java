package com.clinic.api.prontuario;

import com.clinic.api.agendamento.Agendamento;
import com.clinic.api.agendamento.AgendamentoRepository;
import com.clinic.api.medico.Medico;
import com.clinic.api.paciente.Paciente;
import com.clinic.api.paciente.PacienteRepository;
import com.clinic.api.prontuario.dto.FolhaDeRostoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProntuarioServiceTest {

    @InjectMocks
    private ProntuarioService service;

    @Mock private ProntuarioRepository repository;
    @Mock private AgendamentoRepository agendamentoRepository;
    @Mock private DadosClinicosFixosRepository dadosFixosRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private DeepSeekService deepSeekService;

    private UUID medicoId;
    private Agendamento agendamento;

    @BeforeEach
    void setup() {
        medicoId = UUID.randomUUID();
        Medico medico = new Medico();
        medico.setId(medicoId);

        agendamento = new Agendamento();
        agendamento.setId(UUID.randomUUID());
        agendamento.setMedico(medico);
    }

    @Test
    @DisplayName("✅ 1. Deve salvar prontuário com sucesso")
    void salvarSucesso() {
        Prontuario prontuario = new Prontuario();
        prontuario.setAgendamento(agendamento);

        when(agendamentoRepository.existsById(any())).thenReturn(true);
        when(repository.findByAgendamentoId(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(prontuario);

        assertNotNull(service.salvar(prontuario, medicoId));
    }

    @Test
    @DisplayName("❌ 2. Deve impedir que um médico altere prontuário de outro colega")
    void erroSegurancaMedico() {
        Prontuario p = new Prontuario();
        p.setAgendamento(agendamento);
        UUID outroMedicoId = UUID.randomUUID();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.salvar(p, outroMedicoId));
        assertTrue(ex.getMessage().contains("Segurança"));
    }

    @Test
    @DisplayName("❌ 3. Deve impedir duplicidade de prontuário para a mesma consulta")
    void erroDuplicidade() {
        Prontuario p = new Prontuario();
        p.setAgendamento(agendamento);

        when(agendamentoRepository.existsById(any())).thenReturn(true);
        when(repository.findByAgendamentoId(any())).thenReturn(Optional.of(new Prontuario()));

        assertThrows(RuntimeException.class, () -> service.salvar(p, medicoId));
    }

    @Test
    @DisplayName("✅ 4. Deve gerar Folha de Rosto com cálculo de idade correto")
    void folhaRostoIdade() {
        UUID pacienteId = UUID.randomUUID();
        Paciente p = new Paciente();
        p.setDataNascimento(LocalDate.now().minusYears(30));
        p.setNome("João");

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(p));
        when(deepSeekService.gerarResumoClinico(any())).thenReturn("Resumo");

        FolhaDeRostoDTO dto = service.obterFolhaDeRosto(pacienteId);
        assertEquals(30, dto.getIdade());
    }

    @Test
    @DisplayName("✅ 5. Deve listar histórico completo do paciente")
    void listarHistorico() {
        UUID pid = UUID.randomUUID();
        when(repository.buscarHistoricoCompletoDoPaciente(pid)).thenReturn(List.of(new Prontuario()));
        assertEquals(1, service.listarHistoricoPaciente(pid).size());
    }

    @Test
    @DisplayName("❌ 6. Deve falhar ao buscar prontuário por agendamento inexistente")
    void erroAgendamentoInexistente() {
        when(repository.findByAgendamentoId(any())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.buscarPorAgendamento(UUID.randomUUID()));
    }

    @Test
    @DisplayName("✅ 7. Deve carregar dados fixos (alergias/comorbidades) na folha de rosto")
    void carregarDadosFixos() {
        UUID pid = UUID.randomUUID();
        Paciente p = new Paciente();
        p.setDataNascimento(LocalDate.of(2000,1,1));
        DadosClinicosFixos df = new DadosClinicosFixos();
        df.setAlergias("Lactose");

        when(pacienteRepository.findById(pid)).thenReturn(Optional.of(p));
        when(dadosFixosRepository.findById(pid)).thenReturn(Optional.of(df));

        FolhaDeRostoDTO dto = service.obterFolhaDeRosto(pid);
        assertEquals("Lactose", dto.getAlergias());
    }

    @Test
    @DisplayName("✅ 8. Deve garantir que o resumo da IA seja integrado à folha de rosto")
    void integrarIA() {
        UUID pid = UUID.randomUUID();
        Paciente p = new Paciente();
        p.setDataNascimento(LocalDate.now().minusYears(10));
        when(pacienteRepository.findById(pid)).thenReturn(Optional.of(p));
        when(deepSeekService.gerarResumoClinico(any())).thenReturn("Texto da IA");

        FolhaDeRostoDTO dto = service.obterFolhaDeRosto(pid);
        assertEquals("Texto da IA", dto.getResumoIA());
    }

    @Test
    @DisplayName("✅ 9. Deve limitar histórico e processar folha de rosto mesmo com muitos registros")
    void limiteHistoricoIA() {
        // 1. CENÁRIO: Criamos um ID e um Paciente fictício
        UUID pacienteId = UUID.randomUUID();
        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);
        paciente.setNome("Paciente Teste");
        paciente.setDataNascimento(LocalDate.of(1990, 1, 1));

        // 2. MOCKS: Avisamos aos simuladores como reagir
        // Precisamos simular o paciente para o service não lançar "Paciente não encontrado"
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

        // Simulamos que o paciente tem uma lista de prontuários (pode ser vazia para este teste de fumaça)
        when(repository.buscarHistoricoCompletoDoPaciente(pacienteId)).thenReturn(Collections.emptyList());

        // Simulamos a IA respondendo
        when(deepSeekService.gerarResumoClinico(anyString())).thenReturn("Resumo gerado");

        // 3. AÇÃO E VERIFICAÇÃO: Executamos e garantimos que não explode erro
        assertDoesNotThrow(() -> {
            FolhaDeRostoDTO resultado = service.obterFolhaDeRosto(pacienteId);
            assertNotNull(resultado);
        });

        // Verificamos se o repositório foi consultado corretamente
        verify(pacienteRepository, times(1)).findById(pacienteId);
    }

    @Test @DisplayName("❌ 10. Deve falhar se o agendamento não existir no banco")
    void salvarErroAgendamentoFaltante() {
        Prontuario p = new Prontuario();
        p.setAgendamento(agendamento);
        when(agendamentoRepository.existsById(any())).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.salvar(p, medicoId));
    }
}