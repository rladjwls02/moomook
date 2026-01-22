package com.safhao.moomook.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class LlmSchemaProvider {
    private final ObjectMapper objectMapper;
    private final String extractConstraintsSchema;
    private final String generateExplanationsSchema;

    public LlmSchemaProvider(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.objectMapper = objectMapper;
        JsonNode root = loadSchemaJson(resourceLoader);
        this.extractConstraintsSchema = schemaToString(root, "extract_constraints");
        this.generateExplanationsSchema = schemaToString(root, "generate_explanations");
    }

    public String getExtractConstraintsSchema() {
        return extractConstraintsSchema;
    }

    public String getGenerateExplanationsSchema() {
        return generateExplanationsSchema;
    }

    private JsonNode loadSchemaJson(ResourceLoader resourceLoader) {
        Resource resource = resourceLoader.getResource("classpath:OUTPUT_SCHEMA.json");
        try (InputStream inputStream = resource.exists() ? resource.getInputStream() : fallbackSchemaStream()) {
            return objectMapper.readTree(inputStream);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load OUTPUT_SCHEMA.json", ex);
        }
    }

    private InputStream fallbackSchemaStream() throws IOException {
        Path path = Path.of("OUTPUT_SCHEMA.json");
        if (!Files.exists(path)) {
            throw new IllegalStateException("OUTPUT_SCHEMA.json not found in classpath or working directory");
        }
        return Files.newInputStream(path);
    }

    private String schemaToString(JsonNode root, String schemaKey) {
        JsonNode schemaNode = root.path("schemas").path(schemaKey);
        if (schemaNode.isMissingNode() || schemaNode.isNull()) {
            throw new IllegalStateException("Schema not found: " + schemaKey);
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaNode);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to serialize schema: " + schemaKey, ex);
        }
    }
}
