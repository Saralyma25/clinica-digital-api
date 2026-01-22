package com.clinic.api.prontuario.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class DeepSeekService {

    @Value("${deepseek.api.key:}") // Default vazio se não configurado
    private String apiKey;

    private final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();

    public String gerarResumoClinico(String historicoTexto) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "Resumo IA indisponível: Chave de API não configurada.";
        }

        if (historicoTexto == null || historicoTexto.length() < 20) {
            return "Histórico insuficiente para análise inteligente.";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "deepseek-chat");
            body.put("temperature", 0.5); // Mais conservador e técnico

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content",
                    "Você é um médico assistente sênior. Analise o histórico e resuma em PT-BR: doenças crônicas ativas, evolução do quadro e medicamentos em uso. Máximo 400 caracteres."));
            messages.add(Map.of("role", "user", "content", historicoTexto));

            body.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("choices")) {
                List choices = (List) response.getBody().get("choices");
                Map firstChoice = (Map) choices.get(0);
                Map message = (Map) firstChoice.get("message");
                return (String) message.get("content");
            }

            return "Não foi possível interpretar a resposta da IA.";

        } catch (Exception e) {
            System.err.println("Erro ao chamar DeepSeek: " + e.getMessage());
            return "IA indisponível no momento.";
        }
    }
}