package com.clinic.api.paciente;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

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

        Mockito.when(repository.findByCpf(paciente.getCpf())).thenReturn(Optional.of(new Paciente()));

        Assertions.assertThrows(RuntimeException.class, () -> service.cadastrar(paciente));
    }

    @Test
    @DisplayName("✅ Deve salvar paciente novo")
    void sucessoCadastro() {
        Paciente paciente = new Paciente();
        paciente.setCpf("000.000.000-00");
        paciente.setEmail("paciente@email.com");


        Mockito.lenient().when(repository.findByCpf(any())).thenReturn(Optional.empty());
        Mockito.lenient().when(repository.findByEmail(any())).thenReturn(Optional.empty());
        Mockito.lenient().when(repository.save(any())).thenReturn(paciente);

        Assertions.assertNotNull(service.cadastrar(paciente));
    }
}