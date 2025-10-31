package org.sopt.makers.internal.external.slack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlackMessageUtil {

    private final ObjectMapper jsonMapper;

    public JsonNode createTextFieldNode (String text) {
        ObjectNode textField = jsonMapper.createObjectNode();
        textField.put("type", "mrkdwn");
        textField.put("text", text);
        return textField;
    }

    public ObjectNode getObjectNode() {
        return jsonMapper.createObjectNode();
    }

    public ArrayNode getArrayNode() {
        return jsonMapper.createArrayNode();
    }

    public ObjectNode createSection() {
        ObjectNode textField = getObjectNode();
        textField.put("type", "section");
        return textField;
    }

    public ObjectNode createTextField(String message) {
        ObjectNode textField = createSection();
        textField.set("text", createTextFieldNode(message));
        return textField;
    }
}
