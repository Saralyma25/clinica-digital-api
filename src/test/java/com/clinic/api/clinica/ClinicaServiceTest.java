package com.clinic.api.clinica;

import com.clinic.api.clinica.Clinica;
import com.clinic.api.clinica.domain.ClinicaRepository;
import com.clinic.api.clinica.dto.ClinicaRequest;
import com.clinic.api.clinica.dto.ClinicaResponse;
import com.clinic.api.clinica.service.ClinicaService;
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
class ClinicaServiceTest {

    @Mock
    private ClinicaRepository repository;

    @InjectMocks
    private ClinicaService service;

    @Test
    @DisplayName("1. Deve salvar clínica com sucesso")
    void salvarSucesso() {
        ClinicaRequest request = criarRequest();
        when(repository.existsByCnpj(request.getCnpj())).thenReturn(false);
        when(repository.save(any(Clinica.class))).thenAnswer(i -> {
            Clinica c = i.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        ClinicaResponse response = service.salvar(request);

        assertNotNull(response.getId());
        assertEquals("12345678000199", response.getCnpj());
    }

    @Test
    @DisplayName("2. Deve lançar exceção se CNPJ já existe")
    void salvarErroCnpjDuplicado() {
        ClinicaRequest request = criarRequest();
        when(repository.existsByCnpj(request.getCnpj())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.salvar(request));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("3. Deve listar apenas clínicas ativas")
    void listarAtivas() {
        Clinica c1 = new Clinica(); c1.setAtivo(true);
        Clinica c2 = new Clinica(); c2.setAtivo(false); // Inativa

        when(repository.findAll()).thenReturn(List.of(c1, c2));

        List<ClinicaResponse> resultado = service.listar();

        assertEquals(1, resultado.size()); // Só deve vir a ativa
    }

    @Test
    @DisplayName("4. Deve buscar por ID com sucesso")
    void buscarPorIdSucesso() {
        UUID id = UUID.randomUUID();
        Clinica clinica = new Clinica();
        clinica.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(clinica));

        ClinicaResponse response = service.buscarPorId(id);
        assertEquals(id, response.getId());
    }

    @Test
    @DisplayName("5. Deve lançar erro ao buscar ID inexistente")
    void buscarPorIdErro() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.buscarPorId(UUID.randomUUID()));
    }

    @Test
    @DisplayName("6. Deve atualizar clínica com sucesso")
    void atualizarSucesso() {
        UUID id = UUID.randomUUID();
        ClinicaRequest request = criarRequest();
        request.setNomeFantasia("Novo Nome");

        Clinica clinicaAntiga = new Clinica();
        clinicaAntiga.setNomeFantasia("Velho Nome");

        when(repository.findById(id)).thenReturn(Optional.of(clinicaAntiga));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        ClinicaResponse response = service.atualizar(id, request);

        assertEquals("Novo Nome", response.getNomeFantasia());
    }

    @Test
    @DisplayName("7. Deve lançar erro ao atualizar clínica inexistente")
    void atualizarErro() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.atualizar(UUID.randomUUID(), criarRequest()));
    }

    @Test
    @DisplayName("8. Deve realizar exclusão lógica (Soft Delete)")
    void excluirSucesso() {
        UUID id = UUID.randomUUID();
        Clinica clinica = new Clinica();
        clinica.setAtivo(true);

        when(repository.findById(id)).thenReturn(Optional.of(clinica));

        service.excluir(id);

        assertFalse(clinica.getAtivo()); // Deve ter ficado false
        verify(repository).save(clinica); // Deve salvar a alteração
    }

    @Test
    @DisplayName("9. Deve lançar erro ao excluir clínica inexistente")
    void excluirErro() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.excluir(UUID.randomUUID()));
    }

    @Test
    @DisplayName("10. Valida mapeamento correto de campos no salvar")
    void validarMapeamento() {
        ClinicaRequest req = criarRequest();
        req.setRazaoSocial("Razao S.A.");
        req.setEndereco("Rua 1");

        when(repository.existsByCnpj(any())).thenReturn(false);
        when(repository.save(any(Clinica.class))).thenAnswer(i -> i.getArgument(0)); // Retorna o que recebeu

        service.salvar(req);

        verify(repository).save(argThat(clinica ->
                clinica.getRazaoSocial().equals("Razao S.A.") &&
                        clinica.getEndereco().equals("Rua 1") &&
                        clinica.getAtivo() == true
        ));
    }

    private ClinicaRequest criarRequest() {
        ClinicaRequest req = new ClinicaRequest();
        req.setCnpj("12345678000199");
        req.setNomeFantasia("Clinica Teste");
        return req;
    }
}