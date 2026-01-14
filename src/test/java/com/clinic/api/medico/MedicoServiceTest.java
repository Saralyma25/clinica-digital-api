package com.clinic.api.medico;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicoServiceTest {

    @InjectMocks
    private MedicoService service;

    @Mock
    private MedicoRepository repository;

    private Medico medicoPadrao;

    @BeforeEach
    void setup() {
        medicoPadrao = new Medico();
        medicoPadrao.setCrm("12345-SP");
        medicoPadrao.setEmail("medico@clinica.com");
        medicoPadrao.setNome("Dr. Teste");
    }

    @Test
    @DisplayName("❌ 1. Deve barrar CRM duplicado")
    void crmDuplicado() {
        when(repository.findByCrm(anyString())).thenReturn(Optional.of(new Medico()));
        assertThrows(RuntimeException.class, () -> service.cadastrar(medicoPadrao));
    }

    @Test
    @DisplayName("❌ 2. Deve barrar Email duplicado")
    void emailDuplicado() {
        when(repository.findByCrm(anyString())).thenReturn(Optional.empty());
        when(repository.findByEmail(anyString())).thenReturn(Optional.of(new Medico()));
        assertThrows(RuntimeException.class, () -> service.cadastrar(medicoPadrao));
    }

    @Test
    @DisplayName("✅ 3. Deve cadastrar com sucesso")
    void cadastrarSucesso() {
        when(repository.findByCrm(anyString())).thenReturn(Optional.empty());
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(medicoPadrao);

        Medico salvo = service.cadastrar(medicoPadrao);
        assertNotNull(salvo);
        verify(repository, times(1)).save(any());
    }

    @Test
    @DisplayName("✅ 4. Deve buscar por ID com sucesso")
    void buscarIdSucesso() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(medicoPadrao));
        assertNotNull(service.buscarPorId(id));
    }

    @Test
    @DisplayName("❌ 5. Deve dar erro ao buscar ID inexistente")
    void buscarIdErro() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.buscarPorId(UUID.randomUUID()));
    }

    @Test
    @DisplayName("✅ 6. Deve listar todos os médicos")
    void listarTodos() {
        when(repository.findAll()).thenReturn(List.of(medicoPadrao, medicoPadrao));
        assertEquals(2, service.listarTodos().size());
    }

    @Test
    @DisplayName("✅ 7. Deve atualizar dados e configurações de agenda")
    void atualizarSucesso() {
        UUID id = UUID.randomUUID();
        Medico novosDados = new Medico();
        novosDados.setNome("Novo Nome");
        novosDados.setDuracaoConsulta(30);

        when(repository.findById(id)).thenReturn(Optional.of(medicoPadrao));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Medico atualizado = service.atualizar(id, novosDados);
        assertEquals("Novo Nome", atualizado.getNome());
        assertEquals(30, atualizado.getDuracaoConsulta());
    }

    @Test
    @DisplayName("✅ 8. Deve excluir médico com sucesso")
    void excluirSucesso() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);
        assertDoesNotThrow(() -> service.excluir(id));
        verify(repository).deleteById(id);
    }

    @Test
    @DisplayName("❌ 9. Deve falhar ao excluir médico inexistente")
    void excluirErro() {
        when(repository.existsById(any())).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.excluir(UUID.randomUUID()));
    }

    @Test
    @DisplayName("✅ 10. Deve buscar médicos por nome (Contendo texto)")
    void buscarNome() {
        when(repository.findByNomeContainingIgnoreCase("Teste")).thenReturn(List.of(medicoPadrao));
        List<Medico> resultados = service.buscarPorNome("Teste");
        assertFalse(resultados.isEmpty());
    }
}