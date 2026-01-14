package com.clinic.api.plano;

import com.clinic.api.convenio.Convenio;
import com.clinic.api.convenio.ConvenioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanoServiceTest {

    @InjectMocks
    private PlanoService service;

    @Mock
    private PlanoRepository repository;

    @Mock
    private ConvenioRepository convenioRepository;

    @Test
    @DisplayName("❌ Deve bloquear plano com convênio inexistente ou nulo")
    void erroConvenioInexistente() {
        // Cenário 1: Objeto Convênio existe, mas ID não está no banco
        Plano plano = new Plano();
        Convenio convenio = new Convenio();
        convenio.setId(UUID.randomUUID());
        plano.setConvenio(convenio);

        when(convenioRepository.existsById(any())).thenReturn(false);

        RuntimeException erro = assertThrows(RuntimeException.class, () -> service.cadastrar(plano));
        assertEquals("Convênio inválido ou não informado.", erro.getMessage());

        // Cenário 2: Convênio é null
        Plano planoNull = new Plano();
        assertThrows(RuntimeException.class, () -> service.cadastrar(planoNull));
    }

    @Test
    @DisplayName("✅ Deve salvar plano vinculado a convênio real")
    void sucessoPlano() {
        Plano plano = new Plano();
        plano.setNome("Plano Ouro");
        Convenio convenio = new Convenio();
        convenio.setId(UUID.randomUUID());
        plano.setConvenio(convenio);

        when(convenioRepository.existsById(any())).thenReturn(true);
        when(repository.save(any())).thenReturn(plano);

        Plano salvo = service.cadastrar(plano);
        assertNotNull(salvo);
        assertEquals("Plano Ouro", salvo.getNome());
    }

    @Test
    @DisplayName("✅ Deve listar todos os planos")
    void deveListarTodos() {
        when(repository.findAll()).thenReturn(List.of(new Plano(), new Plano()));

        List<Plano> lista = service.listarTodos();
        assertEquals(2, lista.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("✅ Deve listar planos por convênio específico")
    void deveListarPorConvenio() {
        UUID convenioId = UUID.randomUUID();
        when(repository.findByConvenioId(convenioId)).thenReturn(List.of(new Plano()));

        List<Plano> lista = service.listarPorConvenio(convenioId);
        assertFalse(lista.isEmpty());
        assertEquals(1, lista.size());
    }
}