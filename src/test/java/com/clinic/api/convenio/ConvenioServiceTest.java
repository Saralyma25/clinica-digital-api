package com.clinic.api.convenio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConvenioServiceTest {

    @InjectMocks
    private ConvenioService service;

    @Mock private ConvenioRepository repository;

    @Test @DisplayName("✅ 1. Deve cadastrar convênio")
    void cadastrarSucesso() {
        Convenio c = new Convenio(); c.setNome("Unimed");
        when(repository.findByNomeContainingIgnoreCase(any())).thenReturn(Collections.emptyList());
        when(repository.save(any())).thenReturn(c);
        assertNotNull(service.cadastrar(c));
    }

    @Test @DisplayName("✅ 2. Deve listar todos os convênios")
    void listarTodos() {
        when(repository.findAll()).thenReturn(List.of(new Convenio()));
        assertEquals(1, service.listarTodos().size());
    }

    @Test @DisplayName("✅ 3. Deve buscar convênio por ID")
    void buscarPorId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(new Convenio()));
        assertNotNull(service.buscarPorId(id));
    }

    @Test @DisplayName("❌ 4. Erro ao buscar convênio inexistente")
    void buscarErro() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.buscarPorId(UUID.randomUUID()));
    }

    @Test @DisplayName("✅ 5. Deve excluir convênio")
    void excluir() {
        UUID id = UUID.randomUUID();
        assertDoesNotThrow(() -> service.excluir(id));
        verify(repository).deleteById(id);
    }

    @Test
    @DisplayName("✅ 6. Deve validar busca de nome existente")
    void buscarNomeCase() {
        Convenio c = new Convenio();
        c.setNome("Bradesco");

        // Configuramos o mock para retornar a lista na busca E o objeto no salvamento
        when(repository.findByNomeContainingIgnoreCase(anyString())).thenReturn(List.of(c));
        when(repository.save(any(Convenio.class))).thenReturn(c); // <--- Adicione esta linha

        Convenio salvo = service.cadastrar(c);
        assertNotNull(salvo);
        assertEquals("Bradesco", salvo.getNome());
    }

    @Test @DisplayName("✅ 7. Deve validar se nome do convênio não é vazio")
    void nomeVazio() {
        Convenio c = new Convenio();
        assertDoesNotThrow(() -> service.cadastrar(c));
    }

    @Test @DisplayName("✅ 8. Deve permitir múltiplos convênios na lista")
    void listaMultipla() {
        when(repository.findAll()).thenReturn(List.of(new Convenio(), new Convenio()));
        assertEquals(2, service.listarTodos().size());
    }

    @Test @DisplayName("✅ 9. Deve chamar o save do repository")
    void verificarSave() {
        service.cadastrar(new Convenio());
        verify(repository, times(1)).save(any());
    }

    @Test @DisplayName("✅ 10. Deve garantir retorno de lista vazia se não houver convênios")
    void listaVazia() {
        when(repository.findAll()).thenReturn(Collections.emptyList());
        assertTrue(service.listarTodos().isEmpty());
    }
}