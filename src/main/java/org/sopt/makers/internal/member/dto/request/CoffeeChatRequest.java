package org.sopt.makers.internal.member.dto.request;

public record CoffeeChatRequest(
        Long receiverId,
        String senderEmail,
        String category,
        String content
) {}
