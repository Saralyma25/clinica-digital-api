package com.clinic.api.usuario;

import com.clinic.api.usuario.Usuario;
import com.clinic.api.usuario.domain.UserRole;
import com.clinic.api.usuario.domain.UsuarioRepository;
import com.clinic.api.usuario.dto.UsuarioAtualizacaoDto;
import com.clinic.api.usuario.dto.UsuarioCadastroDto;
import com.clinic.api.usuario.dto.UsuarioDetalhamentoDto;
import com.clinic.api.usuario.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
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
class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService service;

    @Mock
    private UsuarioRepository repository;

    // --- 1. TESTES DE CADASTRO ---

    @Test
    @DisplayName("Deve cadastrar usuário com sucesso quando email não existe")
    void deveCadastrarUsuarioComSucesso() {
        UsuarioCadastroDto dados = new UsuarioCadastroDto("teste@clinica.com", "123456", UserRole.PACIENTE);

        when(repository.existsByEmail(dados.email())).thenReturn(false);
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        UsuarioDetalhamentoDto resultado = service.cadastrar(dados);

        assertNotNull(resultado.id());
        assertEquals("teste@clinica.com", resultado.email());
        verify(repository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Não deve cadastrar usuário se o email já estiver em uso")
    void naoDeveCadastrarUsuarioComEmailDuplicado() {
        UsuarioCadastroDto dados = new UsuarioCadastroDto("duplicado@clinica.com", "123456", UserRole.MEDICO);
        when(repository.existsByEmail(dados.email())).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.cadastrar(dados);
        });

        assertEquals("Email já cadastrado.", exception.getMessage());
        verify(repository, never()).save(any(Usuario.class));
    }

    // --- 2. TESTES DE BUSCA ---

    @Test
    @DisplayName("Deve retornar detalhes do usuário quando ID existe")
    void deveBuscarUsuarioPorIdExistente() {
        UUID id = UUID.randomUUID();
        Usuario usuario = criarUsuarioMock(id, "busca@clinica.com");
        when(repository.findById(id)).thenReturn(Optional.of(usuario));

        UsuarioDetalhamentoDto resultado = service.buscarPorId(id);

        assertEquals(id, resultado.id());
        assertEquals("busca@clinica.com", resultado.email());
    }

    @Test
    @DisplayName("Deve lançar exceção quando buscar ID inexistente")
    void deveLancarExceptionAoBuscarIdInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.buscarPorId(id));
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void deveListarTodosUsuarios() {
        List<Usuario> lista = List.of(
                criarUsuarioMock(UUID.randomUUID(), "a@test.com"),
                criarUsuarioMock(UUID.randomUUID(), "b@test.com")
        );
        when(repository.findAll()).thenReturn(lista);

        List<UsuarioDetalhamentoDto> resultado = service.listarTodos();

        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("Deve buscar usuários por termo")
    void deveBuscarUsuariosPorTermo() {
        String termo = "gmail";
        List<Usuario> lista = List.of(criarUsuarioMock(UUID.randomUUID(), "maria@gmail.com"));
        when(repository.findByEmailContainingIgnoreCase(termo)).thenReturn(lista);

        List<UsuarioDetalhamentoDto> resultado = service.buscarPorTermo(termo);

        assertFalse(resultado.isEmpty());
        assertEquals("maria@gmail.com", resultado.get(0).email());
    }

    // --- 3. TESTES DE ATUALIZAÇÃO ---

    @Test
    @DisplayName("Deve atualizar usuário com sucesso (Role e Ativo)")
    void deveAtualizarUsuarioComSucesso() {
        UUID id = UUID.randomUUID();
        Usuario usuarioAntigo = criarUsuarioMock(id, "antigo@clinica.com");

        // DTO: email=null, role=ADMIN, ativo=false
        UsuarioAtualizacaoDto dadosNovos = new UsuarioAtualizacaoDto(null, UserRole.ADMIN, false);

        when(repository.findById(id)).thenReturn(Optional.of(usuarioAntigo));
        // Mock do save retornando o próprio objeto modificado
        when(repository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioDetalhamentoDto resultado = service.atualizar(id, dadosNovos);

        assertEquals(UserRole.ADMIN, resultado.role()); // Mudou
        assertEquals(false, resultado.ativo()); // Mudou
        assertEquals("antigo@clinica.com", resultado.email()); // Manteve
    }

    @Test
    @DisplayName("Deve atualizar email com sucesso se novo email estiver livre")
    void deveAtualizarEmailNovoLivre() {
        UUID id = UUID.randomUUID();
        Usuario usuarioAntigo = criarUsuarioMock(id, "antigo@clinica.com");

        // DTO: email=novo, role=null, ativo=null
        UsuarioAtualizacaoDto dadosNovos = new UsuarioAtualizacaoDto("novo@clinica.com", null, null);

        when(repository.findById(id)).thenReturn(Optional.of(usuarioAntigo));
        when(repository.existsByEmail("novo@clinica.com")).thenReturn(false);
        when(repository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioDetalhamentoDto resultado = service.atualizar(id, dadosNovos);

        assertEquals("novo@clinica.com", resultado.email());
    }

    @Test
    @DisplayName("Não deve atualizar email se já pertencer a outro usuário")
    void naoDeveAtualizarEmailSeJaExistir() {
        UUID id = UUID.randomUUID();
        Usuario usuarioAntigo = criarUsuarioMock(id, "meu@clinica.com");
        UsuarioAtualizacaoDto dadosNovos = new UsuarioAtualizacaoDto("ocupado@clinica.com", null, null);

        when(repository.findById(id)).thenReturn(Optional.of(usuarioAntigo));
        when(repository.existsByEmail("ocupado@clinica.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.atualizar(id, dadosNovos));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar usuário inexistente")
    void deveLancarExceptionAoAtualizarUsuarioInexistente() {
        UUID id = UUID.randomUUID();
        UsuarioAtualizacaoDto dados = new UsuarioAtualizacaoDto("qualquer@mail.com", null, null);
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.atualizar(id, dados));
    }

    // --- 4. TESTES DE EXCLUSÃO ---

    @Test
    @DisplayName("Deve inativar usuário (Exclusão Lógica)")
    void deveInativarUsuarioComSucesso() {
        UUID id = UUID.randomUUID();
        Usuario usuario = criarUsuarioMock(id, "tchau@clinica.com");
        assertTrue(usuario.getAtivo()); // Começa ativo

        when(repository.findById(id)).thenReturn(Optional.of(usuario));

        service.excluir(id);

        assertFalse(usuario.getAtivo()); // Deve estar inativo
        verify(repository).save(usuario); // Verifica se o save foi chamado
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar excluir usuário inexistente")
    void deveLancarErroAoExcluirInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.excluir(id));
    }

    // --- MÉTODOS AUXILIARES ---
    private Usuario criarUsuarioMock(UUID id, String email) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(email);
        u.setSenha("123");
        u.setRole(UserRole.PACIENTE);
        u.setAtivo(true);
        return u;
    }
}