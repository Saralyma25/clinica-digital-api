package com.clinic.api.paciente;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @InjectMocks
    private PacienteService service;

    @Mock
    private PacienteRepository repository;

    @Test
    @DisplayName("❌ Não deve permitir CPF duplicado")
    void erroCpfDuplicado() {
        Paciente paciente = new Paciente();
        paciente.setCpf("111.222.333-44");

        when(repository.findByCpf(paciente.getCpf())).thenReturn(Optional.of(new Paciente()));

        // CORREÇÃO: A variável é 'ex' e a frase deve ser a completa
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.cadastrar(paciente));
        Assertions.assertEquals("Este CPF já está cadastrado no sistema.", ex.getMessage());
    }

    @Test
    @DisplayName("✅ Deve salvar paciente novo com sucesso")
    void sucessoCadastro() {
        Paciente paciente = new Paciente();
        paciente.setCpf("000.000.000-00");
        paciente.setNome("Sara Teste");

        when(repository.findByCpf(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(paciente);

        Paciente salvo = service.cadastrar(paciente);
        assertNotNull(salvo);
        assertEquals("Sara Teste", salvo.getNome());
    }

    @Test
    @DisplayName("✅ Deve buscar paciente por ID existente")
    void deveBuscarPorId() {
        UUID id = UUID.randomUUID();
        Paciente p = new Paciente();
        p.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(p));

        Paciente encontrado = service.buscarPorId(id);
        assertEquals(id, encontrado.getId());
    }

    @Test
    @DisplayName("❌ Deve lançar erro ao buscar ID inexistente")
    void erroBuscarIdInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.buscarPorId(id));
        assertEquals("Paciente não encontrado.", ex.getMessage());
    }

    @Test
    @DisplayName("✅ Deve listar todos os pacientes")
    void deveListarTodos() {
        when(repository.findAll()).thenReturn(List.of(new Paciente(), new Paciente()));
        List<Paciente> lista = service.listarTodos();
        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("✅ Deve atualizar dados do paciente")
    void deveAtualizarPaciente() {
        UUID id = UUID.randomUUID();
        Paciente antigo = new Paciente();
        antigo.setId(id);
        antigo.setNome("Nome Antigo");

        Paciente novosDados = new Paciente();
        novosDados.setNome("Nome Novo");
        novosDados.setEmail("novo@email.com");

        when(repository.findById(id)).thenReturn(Optional.of(antigo));
        when(repository.save(any(Paciente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Paciente atualizado = service.atualizar(id, novosDados);

        assertEquals("Nome Novo", atualizado.getNome());
        assertEquals("novo@email.com", atualizado.getEmail());
    }

    @Test
    @DisplayName("✅ Deve excluir paciente existente")
    void deveExcluir() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        assertDoesNotThrow(() -> service.excluir(id));
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("❌ Deve lançar erro ao tentar excluir paciente inexistente")
    void erroExcluirInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.excluir(id));
        verify(repository, never()).deleteById(id);
    }
}