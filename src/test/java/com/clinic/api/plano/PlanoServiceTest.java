package com.clinic.api.plano.service;

import com.clinic.api.convenio.Convenio;
import com.clinic.api.convenio.domain.ConvenioRepository;
import com.clinic.api.plano.Plano;
import com.clinic.api.plano.domain.PlanoRepository;
import com.clinic.api.plano.dto.PlanoRequest;
import com.clinic.api.plano.dto.PlanoResponse;
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
class PlanoServiceTest {

    @Mock private PlanoRepository repository;
    @Mock private ConvenioRepository convenioRepository;
    @InjectMocks private PlanoService service;

    @Test
    @DisplayName("1. Cadastrar Plano: Sucesso")
    void cadastrarSucesso() {
        UUID convId = UUID.randomUUID();
        PlanoRequest req = new PlanoRequest();
        req.setConvenioId(convId);
        req.setNome("Ouro");
        req.setValorRepasse(BigDecimal.TEN);

        Convenio conv = new Convenio(); conv.setNome("Unimed");

        when(convenioRepository.findById(convId)).thenReturn(Optional.of(conv));
        when(repository.save(any())).thenAnswer(i -> {
            Plano p = i.getArgument(0); p.setId(UUID.randomUUID()); return p;
        });

        PlanoResponse res = service.cadastrar(req);
        assertEquals("Ouro", res.getNome());
        assertEquals("Unimed", res.getNomeConvenio());
    }

    @Test
    @DisplayName("2. Cadastrar Plano: Erro Convênio Inexistente")
    void cadastrarErroConvenio() {
        PlanoRequest req = new PlanoRequest();
        req.setConvenioId(UUID.randomUUID());
        when(convenioRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.cadastrar(req));
    }

    @Test
    @DisplayName("3. Listar Todos: Apenas Ativos")
    void listarAtivos() {
        Plano p1 = new Plano(); p1.setAtivo(true); p1.setConvenio(new Convenio());
        Plano p2 = new Plano(); p2.setAtivo(false); // Inativo

        when(repository.findAll()).thenReturn(List.of(p1, p2));

        List<PlanoResponse> lista = service.listarTodos();
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("4. Listar Por Convênio: Sucesso")
    void listarPorConvenio() {
        UUID id = UUID.randomUUID();
        Plano p = new Plano(); p.setConvenio(new Convenio());

        when(repository.findByConvenioIdAndAtivoTrue(id)).thenReturn(List.of(p));

        assertFalse(service.listarPorConvenio(id).isEmpty());
    }

    @Test
    @DisplayName("5. Excluir: Soft Delete")
    void excluirSucesso() {
        UUID id = UUID.randomUUID();
        Plano p = new Plano(); p.setAtivo(true);

        when(repository.findById(id)).thenReturn(Optional.of(p));

        service.excluir(id);

        assertFalse(p.getAtivo());
        verify(repository).save(p);
    }

    @Test
    @DisplayName("6. Excluir: Erro Inexistente")
    void excluirErro() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.excluir(UUID.randomUUID()));
    }

    @Test
    @DisplayName("7. Validar Valor de Repasse")
    void validarValor() {
        PlanoRequest req = new PlanoRequest();
        req.setConvenioId(UUID.randomUUID());
        req.setValorRepasse(new BigDecimal("150.00"));

        when(convenioRepository.findById(any())).thenReturn(Optional.of(new Convenio()));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        PlanoResponse res = service.cadastrar(req);
        assertEquals(new BigDecimal("150.00"), res.getValorRepasse());
    }

    @Test
    @DisplayName("8. Garantir que plano nasce Ativo")
    void validarAtivo() {
        PlanoRequest req = new PlanoRequest();
        req.setConvenioId(UUID.randomUUID());

        when(convenioRepository.findById(any())).thenReturn(Optional.of(new Convenio()));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        PlanoResponse res = service.cadastrar(req);
        assertTrue(res.getAtivo());
    }

    @Test
    @DisplayName("9. Listar por Convênio retorna vazio se não houver")
    void listarPorConvenioVazio() {
        when(repository.findByConvenioIdAndAtivoTrue(any())).thenReturn(List.of());
        assertTrue(service.listarPorConvenio(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("10. Validação de Integridade DTO")
    void validarDTO() {
        Plano p = new Plano();
        p.setNome("Test");
        p.setValorRepasse(BigDecimal.ONE);
        p.setAtivo(true);
        p.setConvenio(new Convenio());
        p.getConvenio().setNome("Conv");

        PlanoResponse dto = new PlanoResponse(p);
        assertEquals("Conv", dto.getNomeConvenio());
    }
}