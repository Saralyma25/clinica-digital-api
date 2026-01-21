package com.clinic.api.convenio.service;

import com.clinic.api.convenio.Convenio;
import com.clinic.api.convenio.domain.ConvenioRepository;
import com.clinic.api.convenio.dto.ConvenioRequest;
import com.clinic.api.convenio.dto.ConvenioResponse;
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
class ConvenioServiceTest {

    @Mock
    private ConvenioRepository repository;

    @InjectMocks
    private ConvenioService service;

    // --- TESTES DE CADASTRO ---

    @Test
    @DisplayName("1. Deve cadastrar convênio com sucesso")
    void cadastrarSucesso() {
        // Arrange
        ConvenioRequest request = criarRequest("Unimed", "123456");

        when(repository.existsByNomeIgnoreCase(request.getNome())).thenReturn(false);
        when(repository.existsByRegistroAns(request.getRegistroAns())).thenReturn(false);
        when(repository.save(any(Convenio.class))).thenAnswer(i -> {
            Convenio c = i.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        // Act
        ConvenioResponse response = service.cadastrar(request);

        // Assert
        assertNotNull(response.getId());
        assertEquals("Unimed", response.getNome());
        assertEquals("123456", response.getRegistroAns());
        verify(repository).save(any(Convenio.class));
    }

    @Test
    @DisplayName("2. Deve lançar exceção se nome do convênio já existe")
    void cadastrarErroNomeDuplicado() {
        // Arrange
        ConvenioRequest request = criarRequest("Unimed", "123456");
        when(repository.existsByNomeIgnoreCase(request.getNome())).thenReturn(true);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.cadastrar(request));
        assertEquals("Já existe um convênio com o nome: Unimed", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("3. Deve lançar exceção se registro ANS já existe")
    void cadastrarErroAnsDuplicado() {
        // Arrange
        ConvenioRequest request = criarRequest("Bradesco", "999888");
        when(repository.existsByNomeIgnoreCase(request.getNome())).thenReturn(false);
        when(repository.existsByRegistroAns(request.getRegistroAns())).thenReturn(true);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.cadastrar(request));
        assertEquals("Já existe um convênio com este registro ANS.", ex.getMessage());
        verify(repository, never()).save(any());
    }

    // --- TESTES DE LISTAGEM ---

    @Test
    @DisplayName("4. Deve listar todos filtrando apenas os ativos")
    void listarTodosFiltrandoAtivos() {
        // Arrange
        Convenio c1 = new Convenio("Unimed", "111", 30);
        c1.setAtivo(true);

        Convenio c2 = new Convenio("Amil", "222", 30);
        c2.setAtivo(false); // Inativo

        when(repository.findAll()).thenReturn(List.of(c1, c2));

        // Act
        List<ConvenioResponse> lista = service.listarTodos();

        // Assert
        assertEquals(1, lista.size());
        assertEquals("Unimed", lista.get(0).getNome());
    }

    @Test
    @DisplayName("5. Deve retornar lista vazia se não houver convênios")
    void listarTodosVazio() {
        when(repository.findAll()).thenReturn(List.of());

        List<ConvenioResponse> lista = service.listarTodos();

        assertTrue(lista.isEmpty());
    }

    // --- TESTES DE BUSCA POR ID ---

    @Test
    @DisplayName("6. Deve buscar por ID com sucesso")
    void buscarPorIdSucesso() {
        UUID id = UUID.randomUUID();
        Convenio convenio = new Convenio("SulAmerica", "333", 15);
        convenio.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(convenio));

        ConvenioResponse response = service.buscarPorId(id);

        assertEquals(id, response.getId());
        assertEquals("SulAmerica", response.getNome());
    }

    @Test
    @DisplayName("7. Deve lançar erro ao buscar ID inexistente")
    void buscarPorIdNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.buscarPorId(id));
    }

    // --- TESTES DE BUSCA POR NOME ---

    @Test
    @DisplayName("8. Deve buscar por nome filtrando ativos")
    void buscarPorNomeSucesso() {
        String termo = "Uni";
        Convenio c1 = new Convenio("Unimed", "111", 30); c1.setAtivo(true);
        Convenio c2 = new Convenio("Universal", "222", 30); c2.setAtivo(false); // Inativo

        when(repository.findByNomeContainingIgnoreCase(termo)).thenReturn(List.of(c1, c2));

        List<ConvenioResponse> resultado = service.buscarPorNome(termo);

        assertEquals(1, resultado.size());
        assertEquals("Unimed", resultado.get(0).getNome());
    }

    // --- TESTES DE EXCLUSÃO ---

    @Test
    @DisplayName("9. Deve excluir logicamente (Soft Delete)")
    void excluirSucesso() {
        // Arrange
        UUID id = UUID.randomUUID();
        Convenio convenio = new Convenio("Unimed", "111", 30);
        convenio.setAtivo(true);

        when(repository.findById(id)).thenReturn(Optional.of(convenio));

        // Act
        service.excluir(id);

        // Assert
        assertFalse(convenio.getAtivo()); // Verifica se mudou para false
        verify(repository).save(convenio); // Verifica se salvou a alteração
    }

    @Test
    @DisplayName("10. Deve lançar erro ao tentar excluir convênio inexistente")
    void excluirNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.excluir(id));
        verify(repository, never()).save(any());
    }

    // --- Helper ---
    private ConvenioRequest criarRequest(String nome, String ans) {
        ConvenioRequest req = new ConvenioRequest();
        req.setNome(nome);
        req.setRegistroAns(ans);
        req.setDiasParaPagamento(30);
        return req;
    }
}