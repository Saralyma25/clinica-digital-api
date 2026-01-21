package com.clinic.api.medico.service;

import com.clinic.api.medico.Medico;
import com.clinic.api.medico.domain.MedicoRepository;
import com.clinic.api.medico.dto.MedicoBasicoRequest;
import com.clinic.api.medico.dto.MedicoRequest;
import com.clinic.api.medico.dto.MedicoResponse;
import com.clinic.api.medico.enun.Especialidade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicoServiceTest {

    @Mock private MedicoRepository repository;
    @InjectMocks private MedicoService service;

    @Test
    @DisplayName("1. Cadastro Rápido: Sucesso")
    void cadastroRapidoSucesso() {
        MedicoBasicoRequest req = new MedicoBasicoRequest("Dr. Teste", "teste@email.com", UUID.randomUUID());
        when(repository.findByUsuarioEmail(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> {
            Medico m = i.getArgument(0); m.setId(UUID.randomUUID()); return m;
        });

        MedicoResponse resp = service.cadastrarRapido(req);
        assertNotNull(resp.getId());
        assertEquals("Dr. Teste", resp.getNome());
    }

    @Test
    @DisplayName("2. Cadastro Rápido: Erro Email Duplicado")
    void cadastroRapidoErroEmail() {
        MedicoBasicoRequest req = new MedicoBasicoRequest("Dr. Teste", "existe@email.com", UUID.randomUUID());
        when(repository.findByUsuarioEmail(any())).thenReturn(Optional.of(new Medico()));
        assertThrows(RuntimeException.class, () -> service.cadastrarRapido(req));
    }

    @Test
    @DisplayName("3. Cadastro Completo: Sucesso")
    void cadastroCompletoSucesso() {
        MedicoRequest req = new MedicoRequest();
        req.setNome("Dr. House");
        req.setEmail("house@email.com");
        req.setCrm("12345");
        req.setEspecialidade(Especialidade.CARDIOLOGIA);

        when(repository.findByUsuarioEmail(any())).thenReturn(Optional.empty());
        when(repository.existsByCrm(any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        MedicoResponse resp = service.cadastrarCompleto(req);
        assertEquals("12345", resp.getCrm());
    }

    @Test
    @DisplayName("4. Cadastro Completo: Erro CRM Duplicado")
    void cadastroCompletoErroCRM() {
        MedicoRequest req = new MedicoRequest();
        req.setCrm("12345");
        when(repository.findByUsuarioEmail(any())).thenReturn(Optional.empty());
        when(repository.existsByCrm("12345")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> service.cadastrarCompleto(req));
    }

    @Test
    @DisplayName("5. Listar Ativos: Filtra inativos")
    void listarAtivos() {
        Medico m1 = new Medico(); m1.setAtivo(true);
        Medico m2 = new Medico(); m2.setAtivo(false);
        when(repository.findAll()).thenReturn(List.of(m1, m2));

        List<MedicoResponse> lista = service.listarTodosAtivos();
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("6. Buscar por ID: Sucesso")
    void buscarIdSucesso() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(new Medico()));
        assertNotNull(service.buscarPorId(id));
    }

    @Test
    @DisplayName("7. Buscar por CRM: Sucesso")
    void buscarCrmSucesso() {
        when(repository.findByCrm("123")).thenReturn(Optional.of(new Medico()));
        assertNotNull(service.buscarPorCrm("123"));
    }

    @Test
    @DisplayName("8. Buscar por Nome: Sucesso")
    void buscarNomeSucesso() {
        when(repository.findByNomeContainingIgnoreCase("Dr")).thenReturn(List.of(new Medico()));
        assertFalse(service.buscarPorNome("Dr").isEmpty());
    }

    @Test
    @DisplayName("9. Atualizar: Muda status para completo se preencher CRM")
    void atualizarSucesso() {
        UUID id = UUID.randomUUID();
        Medico medico = new Medico();
        medico.setCadastroCompleto(false);

        MedicoRequest req = new MedicoRequest();
        req.setCrm("999");
        req.setEspecialidade(Especialidade.ORTOPEDIA);

        when(repository.findById(id)).thenReturn(Optional.of(medico));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        MedicoResponse resp = service.atualizar(id, req);

        // Verifica se a regra de negócio funcionou
        assertTrue(resp.isCadastroCompleto());
        assertEquals("999", resp.getCrm());
    }

    @Test
    @DisplayName("10. Excluir: Soft Delete")
    void excluirSucesso() {
        UUID id = UUID.randomUUID();
        Medico m = new Medico(); m.setAtivo(true);

        when(repository.findById(id)).thenReturn(Optional.of(m));

        service.excluir(id);

        assertFalse(m.getAtivo());
        verify(repository).save(m);
    }
}