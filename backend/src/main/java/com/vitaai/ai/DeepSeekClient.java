package com.vitaai.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekClient {

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.model}")
    private String model;

    @Value("${ai.temperature:0.7}")
    private double temperature;

    @Value("${ai.max-tokens:2000}")
    private int maxTokens;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class ChatMessage {
        public String role;
        public String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public String chat(List<ChatMessage> messages) {
        try {
            String url = baseUrl + "/chat/completions";

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);

            ArrayNode msgs = objectMapper.createArrayNode();
            for (ChatMessage msg : messages) {
                ObjectNode m = objectMapper.createObjectNode();
                m.put("role", msg.role);
                m.put("content", msg.content);
                msgs.add(m);
            }
            requestBody.set("messages", msgs);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JsonNode json = objectMapper.readTree(response.getBody());
            return json.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            log.error("AI API call failed", e);
            return "抱歉，AI服务暂时不可用，请稍后再试。\n\n【内容为AI诊断，想要更准确诊断，请去正规医院就诊。】";
        }
    }
}
