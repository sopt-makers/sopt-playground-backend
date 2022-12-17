package org.sopt.makers.internal.dto.member;

public record CoffeeChatRequest(
        Long receiverId,
        String senderEmail,
        String category,
        String content
) {}
