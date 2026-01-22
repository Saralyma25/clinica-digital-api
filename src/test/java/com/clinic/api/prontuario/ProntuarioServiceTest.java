package com.clinic.api.prontuario;

import com.clinic.api.agendamento.Agendamento;
import com.clinic.api.agendamento.AgendamentoRepository;
import com.clinic.api.medico.Medico;
import com.clinic.api.medico.enun.Especialidade;
import com.clinic.api.paciente.Paciente;
import com.clinic.api.paciente.domain.PacienteRepository;
import com.clinic.api.prontuario.domain.DadosClinicosFixosRepository;
import com.clinic.api.prontuario.domain.ProntuarioRepository;
import com.clinic.api.prontuario.dto.*;
import com.clinic.api.prontuario.service.DeepSeekService;
import com.clinic.api.prontuario.service.ProntuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProntuarioServiceTest {

    @Mock private ProntuarioRepository repository;
    @Mock private AgendamentoRepository agendamentoRepository;
    @Mock private DadosClinicosFixosRepository dadosFixosRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private DeepSeekService deepSeekService;

    @InjectMocks private ProntuarioService service;

    // --- TESTES DO MÉTODO SALVAR ---

    @Test
    @DisplayName("1. Deve salvar novo prontuário com sucesso")
    void salvarNovoSucesso() {
        UUID medicoId = UUID.randomUUID();
        UUID agendamentoId = UUID.randomUUID();

        Agendamento agendamento = criarAgendamentoMock(agendamentoId, medicoId);
        ProntuarioRequest request = new ProntuarioRequest();
        request.setAgendamentoId(agendamentoId);
        request.setQueixaPrincipal("Dor de cabeça");

        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(repository.findByAgendamentoId(agendamentoId)).thenReturn(Optional.empty()); // Não existe ainda
        when(repository.save(any(Prontuario.class))).thenAnswer(i -> {
            Prontuario p = i.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        ProntuarioResponse response = service.salvar(request, medicoId);

        assertNotNull(response.getId());
        assertEquals("Dor de cabeça", response.getQueixaPrincipal());
        assertEquals("REALIZADO", agendamento.getStatus()); // Verifica se atualizou o status
        verify(agendamentoRepository).save(agendamento);
    }

    @Test
    @DisplayName("2. Deve bloquear edição por médico diferente do agendamento (Segurança)")
    void salvarErroMedicoDiferente() {
        UUID medicoAgendamento = UUID.randomUUID();
        UUID medicoInvasor = UUID.randomUUID();
        UUID agendamentoId = UUID.randomUUID();

        Agendamento agendamento = criarAgendamentoMock(agendamentoId, medicoAgendamento);
        ProntuarioRequest request = new ProntuarioRequest();
        request.setAgendamentoId(agendamentoId);

        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.salvar(request, medicoInvasor));

        assertTrue(ex.getMessage().contains("Acesso negado"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("3. Deve lançar erro se agendamento não existe")
    void salvarErroAgendamentoInexistente() {
        ProntuarioRequest request = new ProntuarioRequest();
        request.setAgendamentoId(UUID.randomUUID());

        when(agendamentoRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.salvar(request, UUID.randomUUID()));
    }

    // --- TESTES DE DADOS FIXOS ---

    @Test
    @DisplayName("4. Deve salvar dados clínicos fixos (Alergias)")
    void salvarDadosFixos() {
        UUID pacienteId = UUID.randomUUID();
        DadosClinicosRequest req = new DadosClinicosRequest(pacienteId, "Diabetes", "Dipirona", "Obs");
        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(dadosFixosRepository.findById(pacienteId)).thenReturn(Optional.empty());

        service.salvarDadosFixos(req);

        verify(dadosFixosRepository).save(argThat(dados ->
                dados.getAlergias().equals("Dipirona") &&
                        dados.getComorbidades().equals("Diabetes")
        ));
    }

    // --- TESTES DE FOLHA DE ROSTO (IA E HISTÓRICO) ---

    @Test
    @DisplayName("5. Folha de Rosto: Deve chamar IA quando histórico >= 2")
    void folhaRostoComIA() {
        UUID pacienteId = UUID.randomUUID();
        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);
        paciente.setNome("João");
        paciente.setDataNascimento(LocalDate.of(1990, 1, 1));

        // Mock Histórico com 2 itens
        List<Prontuario> historico = List.of(
                criarProntuarioMock("Dores"),
                criarProntuarioMock("Febre")
        );

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(repository.buscarHistoricoCompletoDoPaciente(pacienteId)).thenReturn(historico);
        when(deepSeekService.gerarResumoClinico(anyString())).thenReturn("Resumo gerado pela IA");

        FolhaDeRostoDTO dto = service.obterFolhaDeRosto(pacienteId);

        assertEquals("Resumo gerado pela IA", dto.getResumoIA());
        verify(deepSeekService).gerarResumoClinico(anyString());
    }

    @Test
    @DisplayName("6. Folha de Rosto: Não deve chamar IA quando histórico < 2")
    void folhaRostoSemIA() {
        UUID pacienteId = UUID.randomUUID();
        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);
        paciente.setDataNascimento(LocalDate.of(2000, 1, 1));

        // Mock Histórico com 1 item apenas
        List<Prontuario> historico = List.of(criarProntuarioMock("Dores"));

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(repository.buscarHistoricoCompletoDoPaciente(pacienteId)).thenReturn(historico);

        FolhaDeRostoDTO dto = service.obterFolhaDeRosto(pacienteId);

        assertEquals("Sem histórico suficiente para análise.", dto.getResumoIA());
        verifyNoInteractions(deepSeekService); // Garante que a IA não foi acionada (economia de recursos)
    }

    @Test
    @DisplayName("7. Folha de Rosto: Erro Paciente Inexistente")
    void folhaRostoErro() {
        when(pacienteRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.obterFolhaDeRosto(UUID.randomUUID()));
    }

    // --- TESTES DE LEITURA ---

    @Test
    @DisplayName("8. Deve listar histórico completo do paciente")
    void listarHistorico() {
        UUID pacienteId = UUID.randomUUID();
        Prontuario p = criarProntuarioMock("Queixa");

        when(repository.buscarHistoricoCompletoDoPaciente(pacienteId)).thenReturn(List.of(p));

        List<ProntuarioResponse> lista = service.listarHistoricoPaciente(pacienteId);

        assertFalse(lista.isEmpty());
        assertEquals("Queixa", lista.get(0).getQueixaPrincipal());
    }

    @Test
    @DisplayName("9. Deve buscar prontuário por ID do agendamento")
    void buscarPorAgendamentoSucesso() {
        UUID agendamentoId = UUID.randomUUID();
        Prontuario p = criarProntuarioMock("Teste");

        when(repository.findByAgendamentoId(agendamentoId)).thenReturn(Optional.of(p));

        ProntuarioResponse res = service.buscarPorAgendamento(agendamentoId);
        assertNotNull(res);
    }

    @Test
    @DisplayName("10. Deve lançar erro se não houver prontuário para o agendamento")
    void buscarPorAgendamentoErro() {
        when(repository.findByAgendamentoId(any())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.buscarPorAgendamento(UUID.randomUUID()));
        assertEquals("Prontuário ainda não criado para este agendamento.", ex.getMessage());
    }

    // --- Helpers ---

    private Agendamento criarAgendamentoMock(UUID id, UUID medicoId) {
        Agendamento a = new Agendamento();
        a.setId(id);
        a.setStatus("AGENDADO");
        Medico m = new Medico();
        m.setId(medicoId);
        m.setNome("Dr. Mock");
        m.setEspecialidade(Especialidade.CARDIOLOGIA);
        a.setMedico(m);
        a.setDataConsulta(LocalDateTime.now());
        return a;
    }

    private Prontuario criarProntuarioMock(String queixa) {
        Prontuario p = new Prontuario();
        p.setId(UUID.randomUUID());
        p.setQueixaPrincipal(queixa);
        p.setAgendamento(criarAgendamentoMock(UUID.randomUUID(), UUID.randomUUID()));
        return p;
    }
}