package com.clinic.api.paciente;

import com.clinic.api.documento.domain.DocumentoRepository;
import com.clinic.api.paciente.domain.PacienteRepository;
import com.clinic.api.paciente.dto.PacienteBasicoRequest;
import com.clinic.api.paciente.dto.PacienteRequest;
import com.clinic.api.paciente.dto.PacienteResponse;
import com.clinic.api.paciente.service.PacienteService;
import com.clinic.api.prontuario.domain.ProntuarioRepository;
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

    @Mock private PacienteRepository repository;
    @Mock private ProntuarioRepository prontuarioRepository;
    @Mock private DocumentoRepository documentoRepository;

    @InjectMocks private PacienteService service;



    @Test
    @DisplayName("2. Cadastro Rápido: Email Duplicado")
    void cadastroRapidoErro() {
        PacienteBasicoRequest req = new PacienteBasicoRequest("Teste", "t@t.com");
        when(repository.existsByUsuarioEmail(any())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> service.cadastrarRapido(req));
    }



    @Test
    @DisplayName("4. Cadastro Completo: CPF Duplicado")
    void cadastroCompletoErroCPF() {
        PacienteRequest req = new PacienteRequest();
        req.setCpf("123");
        when(repository.existsByCpf("123")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> service.cadastrarCompleto(req));
    }

    @Test
    @DisplayName("5. Listar Todos: Retorna lista")
    void listarTodos() {
        when(repository.findAll()).thenReturn(List.of(new Paciente()));
        assertFalse(service.listarTodos().isEmpty());
    }

    @Test
    @DisplayName("6. Buscar por ID: Sucesso")
    void buscarId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(new Paciente()));
        assertNotNull(service.buscarPorId(id));
    }

    @Test
    @DisplayName("7. Buscar por ID: Erro")
    void buscarIdErro() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.buscarPorId(UUID.randomUUID()));
    }





    @Test
    @DisplayName("10. Cadastro Completo: Telefone")
    void validarTelefone() {
        PacienteRequest req = new PacienteRequest();
        req.setTelefone("999");
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        PacienteResponse res = service.cadastrarCompleto(req);
        assertEquals("999", res.getTelefone());
    }
}