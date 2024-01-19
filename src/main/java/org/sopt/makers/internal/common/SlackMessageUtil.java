package org.sopt.makers.internal.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.val;
import org.springframework.stereotype.Component;

@Component

public class SlackMessageUtil {

    private final ObjectMapper jsonMapper = new ObjectMapper();

    public JsonNode createTextFieldNode (String text) {
        val textField = jsonMapper.createObjectNode();
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
        val textField = getObjectNode();
        textField.put("type", "section");
        return textField;
    }

    public ObjectNode createTextField(String message) {
        val textField = createSection();
        textField.set("text", createTextFieldNode(message));
        return textField;
    }
}
