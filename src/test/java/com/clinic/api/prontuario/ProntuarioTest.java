package com.clinic.api.prontuario;

import com.clinic.api.agendamento.Agendamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProntuarioTest {

    @Test
    @DisplayName("✅ Deve criar instância vazia e setar valores corretamente")
    void testeGettersESetters() {
        // Cenário
        UUID id = UUID.randomUUID();
        String queixa = "Dor de cabeça";
        String diagnostico = "Enxaqueca";
        String prescricao = "Dipirona";
        LocalDateTime agora = LocalDateTime.now();
        Agendamento agendamento = new Agendamento();

        // Ação
        Prontuario prontuario = new Prontuario();
        prontuario.setId(id);
        prontuario.setQueixaPrincipal(queixa);
        prontuario.setDiagnostico(diagnostico);
        prontuario.setPrescricaoMedica(prescricao);
        prontuario.setDataRegistro(agora);
        prontuario.setAgendamento(agendamento);

        // Validação
        assertEquals(id, prontuario.getId());
        assertEquals(queixa, prontuario.getQueixaPrincipal());
        assertEquals(diagnostico, prontuario.getDiagnostico());
        assertEquals(prescricao, prontuario.getPrescricaoMedica());
        assertEquals(agora, prontuario.getDataRegistro());
        assertEquals(agendamento, prontuario.getAgendamento());
    }

    @Test
    @DisplayName("✅ Deve criar instância com construtor de Agendamento")
    void testeConstrutorPersonalizado() {
        Agendamento agendamento = new Agendamento();
        Prontuario prontuario = new Prontuario(agendamento);

        assertNotNull(prontuario);
        assertEquals(agendamento, prontuario.getAgendamento());
    }

    @Test
    @DisplayName("✅ Deve preencher data de registro automaticamente antes de persistir")
    void testePrePersist() {
        Prontuario prontuario = new Prontuario();

        // Antes do persist, deve ser null
        assertNull(prontuario.getDataRegistro());

        // Simula o evento do Hibernate
        prontuario.prePersist();

        // Valida se preencheu
        assertNotNull(prontuario.getDataRegistro());
        // Garante que a data gerada é atual (não é antiga)
        assertTrue(prontuario.getDataRegistro().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("✅ Equals e HashCode devem funcionar baseados no ID")
    void testeEqualsHashCode() {
        UUID id = UUID.randomUUID();

        Prontuario p1 = new Prontuario();
        p1.setId(id);

        Prontuario p2 = new Prontuario();
        p2.setId(id); // Mesmo ID

        Prontuario p3 = new Prontuario();
        p3.setId(UUID.randomUUID()); // ID Diferente

        // Devem ser iguais
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // Devem ser diferentes
        assertNotEquals(p1, p3);
    }
}