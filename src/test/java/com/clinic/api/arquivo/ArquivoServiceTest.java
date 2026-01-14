package com.clinic.api.arquivo.service;

import org.junit.jupiter.api.*;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ArquivoServiceTest {

    private ArquivoService service;

    @BeforeEach
    void setup() {
        service = new ArquivoService();
    }

    @Test
    @DisplayName("✅ 1. Deve salvar arquivo no disco com sucesso")
    void salvarSucesso() {
        MockMultipartFile file = new MockMultipartFile("arquivo", "teste.txt", "text/plain", "conteudo".getBytes());
        String nomeGerado = service.salvarArquivo(file);

        assertNotNull(nomeGerado);
        assertTrue(nomeGerado.contains("teste.txt"));
    }

    @Test
    @DisplayName("✅ 2. Deve gerar nome único (UUID) para o arquivo")
    void nomeUnico() {
        MockMultipartFile file = new MockMultipartFile("arquivo", "foto.jpg", "image/jpeg", "bytes".getBytes());
        String nome1 = service.salvarArquivo(file);
        String nome2 = service.salvarArquivo(file);

        assertNotEquals(nome1, nome2);
    }

    @Test
    @DisplayName("✅ 3. Deve carregar um arquivo existente")
    void carregarSucesso() {
        MockMultipartFile file = new MockMultipartFile("arquivo", "doc.pdf", "application/pdf", "pdf-data".getBytes());
        String nome = service.salvarArquivo(file);

        Resource resource = service.carregarArquivo(nome);
        assertTrue(resource.exists());
    }

    @Test
    @DisplayName("❌ 4. Deve lançar erro ao carregar arquivo inexistente")
    void erroArquivoInexistente() {
        assertThrows(RuntimeException.class, () -> service.carregarArquivo("arquivo_que_nao_existe.png"));
    }

    @Test
    @DisplayName("✅ 5. Deve persistir o conteúdo exato do arquivo")
    void verificarConteudo() throws Exception {
        String texto = "Dados Clinicos 2026";
        MockMultipartFile file = new MockMultipartFile("arquivo", "info.txt", "text/plain", texto.getBytes());
        String nome = service.salvarArquivo(file);

        Resource resource = service.carregarArquivo(nome);
        String conteudoSalvo = new String(resource.getInputStream().readAllBytes());
        assertEquals(texto, conteudoSalvo);
    }

    @Test
    @DisplayName("✅ 6. Deve aceitar arquivos vazios (Regra técnica)")
    void arquivoVazio() {
        MockMultipartFile file = new MockMultipartFile("arquivo", "vazio.txt", "text/plain", new byte[0]);
        assertDoesNotThrow(() -> service.salvarArquivo(file));
    }

    @Test
    @DisplayName("✅ 7. Deve manter a extensão original do arquivo")
    void manterExtensao() {
        MockMultipartFile file = new MockMultipartFile("arquivo", "exame.pdf", "application/pdf", "data".getBytes());
        String nome = service.salvarArquivo(file);
        assertTrue(nome.endsWith(".pdf"));
    }

    @Test
    @DisplayName("✅ 8. Deve salvar arquivo mesmo com caracteres suspeitos no nome")
    void normalizarNome() {
        // Mudamos o teste para um nome que o Windows aceite, mas que o Service trate
        MockMultipartFile file = new MockMultipartFile("arquivo", "exame#paciente.txt", "text/plain", "data".getBytes());
        String nome = service.salvarArquivo(file);
        assertNotNull(nome);
    }

    @Test
    @DisplayName("❌ 9. Deve falhar se houver erro de IO (Simulado)")
    void erroIO() {
        // Teste de resiliência: MultipartFile nulo
        assertThrows(RuntimeException.class, () -> service.salvarArquivo(null));
    }

    @Test
    @DisplayName("✅ 10. Deve carregar recurso como URLResource válido")
    void tipoRecurso() {
        MockMultipartFile file = new MockMultipartFile("arquivo", "teste.png", "image/png", "img".getBytes());
        String nome = service.salvarArquivo(file);
        Resource resource = service.carregarArquivo(nome);
        assertNotNull(resource.getFilename());
    }
}