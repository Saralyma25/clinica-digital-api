package com.clinic.api.prontuario.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeepSeekServiceTest {

    @InjectMocks
    private DeepSeekService service;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        // Injeta o RestTemplate mockado no serviço usando Reflexão
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
        // Define uma chave de API válida por padrão para os testes
        ReflectionTestUtils.setField(service, "apiKey", "sk-teste-123");
    }

    @Test
    @DisplayName("1. Deve retornar mensagem de erro se API Key não estiver configurada")
    void erroSemApiKey() {
        ReflectionTestUtils.setField(service, "apiKey", ""); // Simula chave vazia

        String resultado = service.gerarResumoClinico("Histórico longo do paciente...");

        assertEquals("Resumo indisponível: Chave de API não configurada.", resultado);
        verifyNoInteractions(restTemplate); // Garante que não chamou a API externa
    }

    @Test
    @DisplayName("2. Deve retornar mensagem de erro se API Key for nula")
    void erroApiKeyNula() {
        ReflectionTestUtils.setField(service, "apiKey", null);

        String resultado = service.gerarResumoClinico("Texto...");

        assertEquals("Resumo indisponível: Chave de API não configurada.", resultado);
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("3. Deve retornar aviso se o histórico for muito curto")
    void erroHistoricoCurto() {
        String resultado = service.gerarResumoClinico("Dor de cabeça"); // < 20 caracteres

        assertEquals("Histórico insuficiente para geração de resumo via IA.", resultado);
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("4. Deve retornar aviso se o histórico for nulo")
    void erroHistoricoNulo() {
        String resultado = service.gerarResumoClinico(null);

        assertEquals("Histórico insuficiente para geração de resumo via IA.", resultado);
    }

    @Test
    @DisplayName("5. Deve processar sucesso da API corretamente (Caminho Feliz)")
    void sucessoGeracaoResumo() {
        // Mock da resposta JSON da DeepSeek
        // Estrutura: { choices: [ { message: { content: "Resumo gerado" } } ] }
        Map<String, String> messageContent = Map.of("content", "Paciente com histórico de asma controlado.");
        Map<String, Object> choice = Map.of("message", messageContent);
        Map<String, Object> bodyResponse = Map.of("choices", List.of(choice));

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(bodyResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        String resultado = service.gerarResumoClinico("Paciente relata histórico de asma desde a infância, uso de bombinha SOS.");

        assertEquals("Paciente com histórico de asma controlado.", resultado);
    }

    @Test
    @DisplayName("6. Deve tratar exceção de conexão (API fora do ar)")
    void erroConexaoApi() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("Connection refused"));

        String resultado = service.gerarResumoClinico("Histórico válido para teste de conexão...");

        assertTrue(resultado.contains("IA indisponível no momento"));
    }

    @Test
    @DisplayName("7. Deve tratar resposta com corpo nulo da API")
    void erroCorpoNulo() {
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        String resultado = service.gerarResumoClinico("Histórico válido para teste...");

        assertEquals("Não foi possível interpretar a resposta da IA.", resultado);
    }

    @Test
    @DisplayName("8. Deve tratar resposta sem a lista 'choices' (JSON inválido/erro lógico)")
    void erroJsonSemChoices() {
        // Resposta vazia: {}
        Map<String, Object> bodyResponse = Map.of("error", "token_limit_exceeded");
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(bodyResponse, HttpStatus.BAD_REQUEST);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        String resultado = service.gerarResumoClinico("Histórico válido para teste...");

        assertEquals("Não foi possível interpretar a resposta da IA.", resultado);
    }

    @Test
    @DisplayName("9. Deve enviar o token Bearer corretamente no Header")
    void validarEnvioToken() {
        // Captura o que foi enviado para o RestTemplate
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);

        // Mock de resposta qualquer para não quebrar
        Map<String, String> messageContent = Map.of("content", "OK");
        Map<String, Object> choice = Map.of("message", messageContent);
        Map<String, Object> bodyResponse = Map.of("choices", List.of(choice));
        when(restTemplate.postForEntity(anyString(), captor.capture(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(bodyResponse, HttpStatus.OK));

        service.gerarResumoClinico("Texto de histórico longo para validar token...");

        HttpEntity capturedEntity = captor.getValue();
        // Verifica se o Header Authorization contém o token configurado no setUp()
        assertTrue(capturedEntity.getHeaders().getFirst("Authorization").contains("sk-teste-123"));
    }

    @Test
    @DisplayName("10. Deve enviar o prompt de sistema correto (Personalidade Médica)")
    void validarPromptSistema() {
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);

        Map<String, String> messageContent = Map.of("content", "OK");
        Map<String, Object> choice = Map.of("message", messageContent);
        Map<String, Object> bodyResponse = Map.of("choices", List.of(choice));
        when(restTemplate.postForEntity(anyString(), captor.capture(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(bodyResponse, HttpStatus.OK));

        service.gerarResumoClinico("Histórico do paciente...");

        HttpEntity<Map<String, Object>> capturedEntity = captor.getValue();
        Map<String, Object> body = capturedEntity.getBody();
        List<Map<String, String>> messages = (List<Map<String, String>>) body.get("messages");

        // Verifica se a primeira mensagem é a instrução de sistema (role: system)
        assertEquals("system", messages.get(0).get("role"));
        assertTrue(messages.get(0).get("content").contains("Você é um assistente médico sênior"));
    }
}