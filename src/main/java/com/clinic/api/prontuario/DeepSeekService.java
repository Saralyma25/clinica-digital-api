package com.clinic.api.prontuario;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class DeepSeekService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    private final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();

    public String gerarResumoClinico(String historicoTexto) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "deepseek-chat");

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content",
                    "Você é um assistente médico sênior. Resuma o histórico do paciente em no máximo 5 linhas, destacando: doenças crônicas, evolução dos sintomas e medicamentos citados. Seja técnico e direto."));
            messages.add(Map.of("role", "user", "content", historicoTexto));

            body.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);

            // Navega no JSON de resposta para pegar o texto
            List choices = (List) response.getBody().get("choices");
            Map firstChoice = (Map) choices.get(0);
            Map message = (Map) firstChoice.get("message");

            return (String) message.get("content");

        } catch (Exception e) {
            return "Não foi possível gerar o resumo automático no momento.";
        }
    }
}