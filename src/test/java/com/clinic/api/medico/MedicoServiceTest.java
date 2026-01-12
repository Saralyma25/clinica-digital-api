package com.clinic.api.medico;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class MedicoServiceTest {

    @InjectMocks
    private MedicoService service;

    @Mock
    private MedicoRepository repository;

    @Test
    @DisplayName("❌ Deve falhar ao cadastrar CRM duplicado")
    void naoDeveDuplicarCrm() {
        Medico medico = new Medico();
        medico.setCrm("12345-SP");

        // Mock: O banco diz "Sim, já achei alguém com esse CRM"
        Mockito.when(repository.findByCrm("12345-SP")).thenReturn(Optional.of(new Medico()));

        RuntimeException erro = assertThrows(RuntimeException.class, () -> service.cadastrar(medico));
        Assertions.assertEquals("Já existe um médico com este CRM.", erro.getMessage());
    }

    @Test
    @DisplayName("✅ Deve cadastrar médico com sucesso")
    void deveCadastrarSucesso() {
        Medico medico = new Medico();
        medico.setCrm("99999-SP");
        medico.setEmail("doutor@teste.com");

        // Mock: Não achou ninguém com esse CRM nem com esse Email
        Mockito.when(repository.findByCrm(any())).thenReturn(Optional.empty());
        Mockito.when(repository.findByEmail(any())).thenReturn(Optional.empty());
        Mockito.when(repository.save(any())).thenReturn(medico);

        Medico salvo = service.cadastrar(medico);
        Assertions.assertNotNull(salvo);
    }
}