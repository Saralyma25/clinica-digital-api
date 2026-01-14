package com.clinic.api.prontuario;

import com.clinic.api.agendamento.Agendamento;
import com.clinic.api.agendamento.AgendamentoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Mock
    private ProntuarioRepository repository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    // --- TESTES DE SALVAR (Cenários de Sucesso e Erro) ---

    @Test
    @DisplayName("✅ Deve salvar um prontuário novo com sucesso")
    void deveSalvarProntuarioNovo() {
        // Cenário
        UUID agendamentoId = UUID.randomUUID();

        Agendamento agendamento = new Agendamento();
        agendamento.setId(agendamentoId);

        Prontuario prontuario = new Prontuario();
        prontuario.setId(UUID.randomUUID());
        prontuario.setAgendamento(agendamento);
        prontuario.setQueixaPrincipal("Dor de cabeça");

        // Mocks
        when(agendamentoRepository.existsById(agendamentoId)).thenReturn(true);
        when(repository.findByAgendamentoId(agendamentoId)).thenReturn(Optional.empty()); // Não existe anterior
        when(repository.save(any(Prontuario.class))).thenReturn(prontuario);

        // Execução
        Prontuario salvo = service.salvar(prontuario);

        // Validação
        assertNotNull(salvo);
        assertEquals("Dor de cabeça", salvo.getQueixaPrincipal());
        verify(repository, times(1)).save(prontuario);
    }

    @Test
    @DisplayName("✅ Deve permitir atualizar um prontuário existente (mesmo ID)")
    void deveAtualizarProntuarioExistente() {
        // Cenário
        UUID agendamentoId = UUID.randomUUID();
        UUID prontuarioId = UUID.randomUUID();

        Agendamento agendamento = new Agendamento();
        agendamento.setId(agendamentoId);

        Prontuario prontuarioParaAtualizar = new Prontuario();
        prontuarioParaAtualizar.setId(prontuarioId); // Mesmo ID
        prontuarioParaAtualizar.setAgendamento(agendamento);

        // O banco retorna um registro, MAS com o mesmo ID
        when(agendamentoRepository.existsById(agendamentoId)).thenReturn(true);
        when(repository.findByAgendamentoId(agendamentoId)).thenReturn(Optional.of(prontuarioParaAtualizar));
        when(repository.save(any(Prontuario.class))).thenReturn(prontuarioParaAtualizar);

        // Execução
        assertDoesNotThrow(() -> service.salvar(prontuarioParaAtualizar));
        verify(repository, times(1)).save(prontuarioParaAtualizar);
    }

    @Test
    @DisplayName("❌ Deve lançar erro ao tentar salvar sem Agendamento vinculado")
    void erroSemAgendamento() {
        Prontuario prontuario = new Prontuario();
        // Agendamento é null aqui

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.salvar(prontuario));
        assertTrue(exception.getMessage().contains("Agendamento inválido"));
    }

    @Test
    @DisplayName("❌ Deve lançar erro se o Agendamento não existe no banco")
    void erroAgendamentoInexistente() {
        UUID idFalso = UUID.randomUUID();
        Agendamento agendamento = new Agendamento();
        agendamento.setId(idFalso);

        Prontuario prontuario = new Prontuario();
        prontuario.setAgendamento(agendamento);

        // Mock diz que NÃO existe
        when(agendamentoRepository.existsById(idFalso)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.salvar(prontuario));
    }

    @Test
    @DisplayName("❌ Não deve permitir dois prontuários diferentes para a mesma consulta")
    void erroProntuarioDuplicado() {
        UUID agendamentoId = UUID.randomUUID();

        Agendamento agendamento = new Agendamento();
        agendamento.setId(agendamentoId);

        // Prontuário NOVO tentando entrar
        Prontuario novoProntuario = new Prontuario();
        novoProntuario.setId(UUID.randomUUID()); // ID A
        novoProntuario.setAgendamento(agendamento);

        // Prontuário VELHO que já existe no banco
        Prontuario prontuarioExistente = new Prontuario();
        prontuarioExistente.setId(UUID.randomUUID()); // ID B (Diferente de A)

        when(agendamentoRepository.existsById(agendamentoId)).thenReturn(true);
        when(repository.findByAgendamentoId(agendamentoId)).thenReturn(Optional.of(prontuarioExistente));

        // Execução e Validação
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.salvar(novoProntuario));
        assertTrue(ex.getMessage().contains("Já existe um prontuário registrado"));
    }

    // --- TESTES DE BUSCA ---

    @Test
    @DisplayName("✅ Deve buscar prontuário por ID do agendamento")
    void deveBuscarPorAgendamento() {
        UUID agendamentoId = UUID.randomUUID();
        Prontuario prontuario = new Prontuario();

        when(repository.findByAgendamentoId(agendamentoId)).thenReturn(Optional.of(prontuario));

        Prontuario encontrado = service.buscarPorAgendamento(agendamentoId);
        assertNotNull(encontrado);
    }

    @Test
    @DisplayName("❌ Deve lançar erro se não achar prontuário pelo agendamento")
    void erroBuscarPorAgendamentoNaoEncontrado() {
        UUID agendamentoId = UUID.randomUUID();

        when(repository.findByAgendamentoId(agendamentoId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.buscarPorAgendamento(agendamentoId));
    }

    @Test
    @DisplayName("✅ Deve buscar prontuário por ID")
    void deveBuscarPorId() {
        UUID id = UUID.randomUUID();
        Prontuario p = new Prontuario();
        p.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(p));

        Prontuario result = service.buscarPorId(id);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("❌ Deve lançar erro se ID não existir")
    void erroBuscarPorIdInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.buscarPorId(id));
    }

    @Test
    @DisplayName("✅ Deve listar histórico do paciente")
    void deveListarHistorico() {
        UUID pacienteId = UUID.randomUUID();
        when(repository.buscarHistoricoCompletoDoPaciente(pacienteId)).thenReturn(List.of(new Prontuario(), new Prontuario()));

        List<Prontuario> lista = service.listarHistoricoPaciente(pacienteId);
        assertEquals(2, lista.size());
    }
}