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

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ProntuarioServiceTest {

    @InjectMocks
    private ProntuarioService service;

    @Mock
    private ProntuarioRepository repository;
    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Test
    @DisplayName("❌ Deve exigir um agendamento válido")
    void erroSemAgendamento() {
        Prontuario prontuario = new Prontuario();
        prontuario.setAgendamento(new Agendamento());
        prontuario.getAgendamento().setId(UUID.randomUUID());

        Mockito.when(agendamentoRepository.existsById(any())).thenReturn(false);

        Assertions.assertThrows(RuntimeException.class, () -> service.salvar(prontuario));
    }

    @Test
    @DisplayName("❌ Não deve permitir dois prontuários para a mesma consulta")
    void erroProntuarioDuplicado() {
        UUID agendamentoId = UUID.randomUUID();
        Prontuario novo = new Prontuario();
        novo.setAgendamento(new Agendamento());
        novo.getAgendamento().setId(agendamentoId);
        novo.setId(UUID.randomUUID()); // ID Novo

        // Cenário: Já existe um prontuário diferente salvo para este agendamento
        Prontuario existente = new Prontuario();
        existente.setId(UUID.randomUUID()); // ID Antigo

        Mockito.when(agendamentoRepository.existsById(any())).thenReturn(true);
        Mockito.when(repository.findByAgendamentoId(agendamentoId)).thenReturn(Optional.of(existente));

        Assertions.assertThrows(RuntimeException.class, () -> service.salvar(novo));
    }
}