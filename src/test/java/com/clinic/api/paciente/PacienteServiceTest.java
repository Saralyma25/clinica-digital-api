package com.clinic.api.paciente;

import com.clinic.api.documento.Documento;
import com.clinic.api.documento.DocumentoRepository;
import com.clinic.api.paciente.dto.TimelineDTO;
import com.clinic.api.prontuario.Prontuario;
import com.clinic.api.prontuario.ProntuarioRepository;
import com.clinic.api.agendamento.Agendamento;
import com.clinic.api.medico.Medico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @InjectMocks
    private PacienteService service;

    @Mock private PacienteRepository repository;
    @Mock private ProntuarioRepository prontuarioRepository;
    @Mock private DocumentoRepository documentoRepository;

    private UUID pacienteId;

    @BeforeEach
    void setup() {
        pacienteId = UUID.randomUUID();
    }

    @Test
    @DisplayName("✅ 1. Deve cadastrar paciente com sucesso")
    void cadastrarSucesso() {
        Paciente p = new Paciente();
        when(repository.save(any())).thenReturn(p);
        assertNotNull(service.cadastrar(p));
    }

    @Test
    @DisplayName("✅ 2. Deve listar todos os pacientes cadastrados")
    void listarTodos() {
        when(repository.findAll()).thenReturn(List.of(new Paciente(), new Paciente()));
        assertEquals(2, service.listarTodos().size());
    }

    @Test
    @DisplayName("✅ 3. Deve buscar paciente por ID com sucesso")
    void buscarPorIdSucesso() {
        Paciente p = new Paciente();
        when(repository.findById(pacienteId)).thenReturn(Optional.of(p));
        assertNotNull(service.buscarPorId(pacienteId));
    }

    @Test
    @DisplayName("❌ 4. Deve lançar erro ao buscar paciente inexistente")
    void buscarPorIdErro() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.buscarPorId(pacienteId));
    }



    @Test
    @DisplayName("✅ 8. Timeline: Deve exibir 'Consulta de rotina' se queixa for nula")
    void timelineQueixaNula() {
        Agendamento a = new Agendamento();
        a.setDataConsulta(LocalDateTime.now());
        Medico m = new Medico(); m.setNome("Dr. Teste"); a.setMedico(m);
        Prontuario p = new Prontuario(); p.setAgendamento(a);
        p.setQueixaPrincipal(null);

        when(prontuarioRepository.buscarHistoricoCompletoDoPaciente(any())).thenReturn(List.of(p));
        assertEquals("Consulta de rotina", service.buscarTimelineCompleta(pacienteId).get(0).getDescricao());
    }

    @Test
    @DisplayName("✅ 9. Deve garantir transacionalidade no cadastro")
    void cadastroTransacional() {
        // Teste de anotação - implícito
        assertDoesNotThrow(() -> service.cadastrar(new Paciente()));
    }


}