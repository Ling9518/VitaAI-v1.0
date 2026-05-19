package com.vitaai.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
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

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeepSeekClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);   // 10s
        factory.setReadTimeout(120_000);     // 120s for long medical responses
        this.restTemplate = new RestTemplate(factory);
    }

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
            String url = baseUrl + "/v1/messages";

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);

            // Extract system message for top-level field
            for (ChatMessage msg : messages) {
                if ("system".equals(msg.role)) {
                    requestBody.put("system", msg.content);
                    break;
                }
            }

            // Build messages array (user + assistant only)
            ArrayNode msgs = objectMapper.createArrayNode();
            for (ChatMessage msg : messages) {
                if ("system".equals(msg.role)) continue;
                ObjectNode m = objectMapper.createObjectNode();
                m.put("role", msg.role);
                m.put("content", msg.content);
                msgs.add(m);
            }
            requestBody.set("messages", msgs);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JsonNode json = objectMapper.readTree(response.getBody());
            for (JsonNode block : json.path("content")) {
                if ("text".equals(block.path("type").asText())) {
                    return block.path("text").asText();
                }
            }
            return "";

        } catch (Exception e) {
            log.error("AI API call failed", e);
            return "抱歉，AI服务暂时不可用，请稍后再试。\n\n【内容为AI诊断，想要更准确诊断，请去正规医院就诊。】";
        }
    }
}
