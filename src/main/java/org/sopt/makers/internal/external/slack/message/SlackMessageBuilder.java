package org.sopt.makers.internal.external.slack.message;

import com.fasterxml.jackson.databind.JsonNode;

public interface SlackMessageBuilder {
    JsonNode buildMessage();
}
