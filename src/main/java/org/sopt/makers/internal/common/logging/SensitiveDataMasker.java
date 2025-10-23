package org.sopt.makers.internal.common.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensitiveDataMasker {

    private static final String MASK = "****";
    private final ObjectMapper objectMapper;

    private static final List<String> SENSITIVE_FIELDS = Arrays.asList(
            "token",
            "accesstoken",
            "refreshtoken",
            "idtoken",

            "authorization",
            "code",

            "phone",
            "email",

            "secret",
            "apikey",
            "api-key"
    );

    public Map<String, String> maskHeaders(Map<String, String> headers) {
        if (headers == null) {
            return null;
        }

        headers.replaceAll((key, value) -> {
            if (isSensitiveField(key)) {
                return MASK;
            }
            return value;
        });

        return headers;
    }

    public String maskJsonString(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return jsonString;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            maskJsonNode(rootNode);
            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception exception) {
            log.debug("Failed to parse JSON for masking: {}", exception.getMessage());
            return jsonString;
        }
    }

    private static void maskJsonNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                JsonNode fieldValue = field.getValue();

                if (isSensitiveField(fieldName)) {
                    objectNode.put(fieldName, MASK);
                } else if (fieldValue.isObject() || fieldValue.isArray()) {
                    maskJsonNode(fieldValue);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                maskJsonNode(arrayElement);
            }
        }
    }

    private static boolean isSensitiveField(String fieldName) {
        if (fieldName == null) return false;

        String lowerFieldName = fieldName.toLowerCase();
        return SENSITIVE_FIELDS.stream()
                .anyMatch(lowerFieldName::contains);
    }
}
