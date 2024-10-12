package org.sopt.makers.internal.dto.member;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CoffeeChatRequest(
        Long receiverId,
        @JsonProperty(required = false) String senderEmail,
        @JsonProperty(required = false) String senderPhone,
        String category,
        String content
) {}
