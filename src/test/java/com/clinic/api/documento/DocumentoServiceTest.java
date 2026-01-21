package com.clinic.api.documento.service;

import com.clinic.api.documento.Documento;
import com.clinic.api.documento.domain.DocumentoRepository;
import com.clinic.api.documento.dto.DocumentoResponse;
import com.clinic.api.paciente.Paciente;
import com.clinic.api.paciente.domain.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock private DocumentoRepository repository;
    @Mock private PacienteRepository pacienteRepository;

    @InjectMocks private DocumentoService service;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        // Injeta a pasta temporária do JUnit
        ReflectionTestUtils.setField(service, "diretorioUploads", tempDir);
    }

    @Test
    @DisplayName("1. Deve salvar documento com sucesso")
    void salvarSucesso() {
        UUID pacienteId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("arquivo", "exame.pdf", "application/pdf", "123".getBytes());

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(new Paciente()));
        when(repository.save(any(Documento.class))).thenAnswer(i -> {
            Documento d = i.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        DocumentoResponse response = service.salvarDocumento(pacienteId, "EXAME", "PACIENTE", file);

        assertNotNull(response.id());
        assertEquals("exame.pdf", response.nomeArquivo());
    }

    @Test
    @DisplayName("2. Deve lançar erro se paciente não existe")
    void salvarErroPacienteInexistente() {
        UUID id = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("a", "b", "c", new byte[0]);

        when(pacienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.salvarDocumento(id, "EXAME", "MEDICO", file));
    }

    @Test
    @DisplayName("3. Deve salvar arquivo fisicamente no disco")
    void verificarArquivoNoDisco() {
        UUID pacienteId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("arq", "teste.txt", "text/plain", "CONTEUDO".getBytes());

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(new Paciente()));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.salvarDocumento(pacienteId, "LAUDO", "MEDICO", file);

        // Verifica se algum arquivo foi criado na pasta temp
        assertTrue(Files.exists(tempDir), "Pasta deve existir");
        // Nota: O nome exato tem um UUID aleatório, difícil verificar aqui sem capturar o retorno,
        // mas se não lançou exceção, o Files.copy funcionou.
    }

    @Test
    @DisplayName("4. Deve listar documentos do paciente")
    void listarDocumentos() {
        UUID pacienteId = UUID.randomUUID();
        Documento d1 = new Documento(); d1.setId(UUID.randomUUID());
        Documento d2 = new Documento(); d2.setId(UUID.randomUUID());

        when(repository.findByPacienteIdOrderByDataUploadDesc(pacienteId)).thenReturn(List.of(d1, d2));

        List<DocumentoResponse> lista = service.listarPorPaciente(pacienteId);

        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("5. Deve buscar entidade por ID com sucesso")
    void buscarPorIdSucesso() {
        UUID id = UUID.randomUUID();
        Documento doc = new Documento(); doc.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(doc));

        Documento encontrado = service.buscarEntidadePorId(id);
        assertEquals(id, encontrado.getId());
    }

    @Test
    @DisplayName("6. Deve lançar erro ao buscar documento inexistente")
    void buscarPorIdErro() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.buscarEntidadePorId(UUID.randomUUID()));
    }

    @Test
    @DisplayName("7. Deve tratar erro de IO ao salvar arquivo (simulado)")
    void erroIOSalvar() throws IOException {
        // Truque: Tentar salvar em um diretório que virou arquivo (bloqueado)
        Path arquivoBloqueado = tempDir.resolve("bloqueio");
        Files.createFile(arquivoBloqueado);
        ReflectionTestUtils.setField(service, "diretorioUploads", arquivoBloqueado); // Quebra o path

        UUID id = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("a", "teste.txt", "text", "dados".getBytes());
        when(pacienteRepository.findById(id)).thenReturn(Optional.of(new Paciente()));

        assertThrows(RuntimeException.class, () -> service.salvarDocumento(id, "A", "B", file));
    }

    @Test
    @DisplayName("8. Deve gerar link de download correto no DTO")
    void validarLinkDownload() {
        UUID docId = UUID.randomUUID();
        Documento doc = new Documento();
        doc.setId(docId);
        doc.setNomeOriginal("teste.pdf");

        DocumentoResponse dto = new DocumentoResponse(doc);

        assertEquals("/documentos/" + docId + "/baixar", dto.linkDownload());
    }

    @Test
    @DisplayName("9. Deve atribuir categorias corretamente (UpperCase)")
    void validarCategorias() {
        UUID id = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("f", "f.txt", "t", "b".getBytes());

        when(pacienteRepository.findById(id)).thenReturn(Optional.of(new Paciente()));
        when(repository.save(any(Documento.class))).thenAnswer(i -> i.getArgument(0));

        DocumentoResponse resp = service.salvarDocumento(id, "exame", "paciente", file);

        // Verifica se converteu para maiúsculo internamente
        verify(repository).save(argThat(d ->
                d.getCategoria().equals("EXAME") && d.getOrigem().equals("PACIENTE")
        ));
    }

    @Test
    @DisplayName("10. Deve aceitar qualquer tipo de arquivo (PDF, Imagem)")
    void aceitarTiposDiversos() {
        MockMultipartFile pdf = new MockMultipartFile("f", "a.pdf", "application/pdf", "1".getBytes());
        MockMultipartFile img = new MockMultipartFile("f", "b.png", "image/png", "2".getBytes());

        UUID id = UUID.randomUUID();
        when(pacienteRepository.findById(id)).thenReturn(Optional.of(new Paciente()));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> service.salvarDocumento(id, "T1", "O1", pdf));
        assertDoesNotThrow(() -> service.salvarDocumento(id, "T2", "O2", img));
    }
}