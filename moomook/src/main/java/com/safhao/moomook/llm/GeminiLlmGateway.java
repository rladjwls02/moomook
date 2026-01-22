package com.safhao.moomook.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safhao.moomook.domain.Menu;
import com.safhao.moomook.dto.ExtractConstraintsResponse;
import com.safhao.moomook.dto.GenerateExplanationsResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "llm", name = "provider", havingValue = "gemini")
@ConditionalOnExpression("'${llm.api-key:}' != ''")
public class GeminiLlmGateway implements LlmGateway {
    private static final String CONSTRAINTS_SYSTEM_PROMPT =
        "You are a strict JSON generator. Return ONLY valid JSON. The JSON must exactly match the provided schema. "
            + "If uncertain, use defaults and put details in notes.";
    private static final String EXPLANATIONS_SYSTEM_PROMPT =
        "You are a strict JSON generator. Return ONLY valid JSON. The JSON must exactly match the provided schema. "
            + "Only mention menus from the provided list. Do not invent items.";

    private final LlmProperties properties;
    private final LlmSchemaProvider schemaProvider;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiLlmGateway(LlmProperties properties, LlmSchemaProvider schemaProvider, ObjectMapper objectMapper) {
        this.properties = properties;
        this.schemaProvider = schemaProvider;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    @Override
    public ExtractConstraintsResponse extractConstraints(String userText) {
        String prompt = buildConstraintsPrompt(userText);
        String responseText = callGemini(CONSTRAINTS_SYSTEM_PROMPT, prompt);
        return parseResponse(responseText, ExtractConstraintsResponse.class, "extract_constraints");
    }

    @Override
    public GenerateExplanationsResponse generateExplanations(String userText,
                                                             List<Menu> menus,
                                                             ExtractConstraintsResponse constraints) {
        String prompt = buildExplanationsPrompt(userText, menus);
        String responseText = callGemini(EXPLANATIONS_SYSTEM_PROMPT, prompt);
        return parseResponse(responseText, GenerateExplanationsResponse.class, "generate_explanations");
    }

    private String buildConstraintsPrompt(String userText) {
        return "User input: \"%s\"%n%nReturn JSON that matches this schema:%n%s"
            .formatted(safeText(userText), schemaProvider.getExtractConstraintsSchema());
    }

    private String buildExplanationsPrompt(String userText, List<Menu> menus) {
        String menuSummary = buildMenuSummary(menus);
        return "User input: \"%s\"%n%nMenus (ONLY use these):%n%s%n%nReturn JSON that matches this schema:%n%s"
            .formatted(safeText(userText), menuSummary, schemaProvider.getGenerateExplanationsSchema());
    }

    private String buildMenuSummary(List<Menu> menus) {
        List<Map<String, Object>> summary = menus.stream()
            .map(menu -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("menuId", menu.getId());
                item.put("name", menu.getName());
                item.put("tags", menu.getTags());
                item.put("spicy_level", menu.getSpicyLevel());
                item.put("cook_time_min", menu.getCookTimeMin());
                item.put("price", menu.getPrice());
                return item;
            })
            .collect(Collectors.toList());
        try {
            return objectMapper.writeValueAsString(summary);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize menu summary", ex);
        }
    }

    private String callGemini(String systemPrompt, String userPrompt) {
        String apiKey = Objects.requireNonNull(properties.getApiKey(), "LLM apiKey is required");
        String model = Objects.requireNonNull(properties.getModel(), "LLM model is required");
        String baseUrl = Objects.requireNonNull(properties.getBaseUrl(), "LLM baseUrl is required");
        URI uri = URI.create("%s/models/%s:generateContent?key=%s".formatted(baseUrl, model, apiKey));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))));
        payload.put("contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", userPrompt)))));
        payload.put("generationConfig", Map.of(
            "temperature", 0.2,
            "topP", 0.1,
            "maxOutputTokens", 1024
        ));

        String body;
        try {
            body = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize Gemini request", ex);
        }

        HttpRequest request = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(20))
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Gemini API error: " + response.statusCode() + " " + response.body());
            }
            return extractTextFromResponse(response.body());
        } catch (IOException ex) {
            throw new IllegalStateException("Gemini API call failed", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gemini API call interrupted", ex);
        }
    }

    private String extractTextFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new IllegalStateException("Gemini response missing candidates");
            }
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                throw new IllegalStateException("Gemini response missing content parts");
            }
            StringBuilder builder = new StringBuilder();
            for (JsonNode part : parts) {
                if (part.has("text")) {
                    builder.append(part.get("text").asText());
                }
            }
            String text = builder.toString().trim();
            if (text.isEmpty()) {
                throw new IllegalStateException("Gemini response text is empty");
            }
            return text;
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to parse Gemini response", ex);
        }
    }

    private <T> T parseResponse(String rawText, Class<T> targetType, String schemaName) {
        String jsonText = stripJsonCodeFence(rawText);
        try {
            return objectMapper.readValue(jsonText, targetType);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to parse " + schemaName + " response", ex);
        }
    }

    private String stripJsonCodeFence(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstLineEnd = trimmed.indexOf('\n');
            if (firstLineEnd > -1) {
                trimmed = trimmed.substring(firstLineEnd + 1);
            }
            int lastFence = trimmed.lastIndexOf("```");
            if (lastFence > -1) {
                trimmed = trimmed.substring(0, lastFence);
            }
        }
        return trimmed.trim();
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }
}
