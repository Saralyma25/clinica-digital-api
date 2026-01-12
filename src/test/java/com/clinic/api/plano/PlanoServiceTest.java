package com.clinic.api.plano;

import com.clinic.api.convenio.Convenio;
import com.clinic.api.convenio.ConvenioRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class PlanoServiceTest {

    @InjectMocks
    private PlanoService service;

    @Mock
    private PlanoRepository repository;
    @Mock
    private ConvenioRepository convenioRepository;

    @Test
    @DisplayName("❌ Deve bloquear plano com convênio inexistente")
    void erroConvenioInexistente() {
        Plano plano = new Plano();
        plano.setConvenio(new Convenio());
        plano.getConvenio().setId(UUID.randomUUID());

        // Mock: Banco diz que esse ID de convênio NÃO existe
        Mockito.when(convenioRepository.existsById(any())).thenReturn(false);

        RuntimeException erro = Assertions.assertThrows(RuntimeException.class, () -> service.cadastrar(plano));
        Assertions.assertEquals("Convênio inválido ou não informado.", erro.getMessage());
    }

    @Test
    @DisplayName("✅ Deve salvar plano vinculado a convênio real")
    void sucessoPlano() {
        Plano plano = new Plano();
        plano.setConvenio(new Convenio());
        plano.getConvenio().setId(UUID.randomUUID());

        // Mock: Banco diz que o convênio existe
        Mockito.when(convenioRepository.existsById(any())).thenReturn(true);
        Mockito.when(repository.save(any())).thenReturn(plano);

        Assertions.assertNotNull(service.cadastrar(plano));
    }
}