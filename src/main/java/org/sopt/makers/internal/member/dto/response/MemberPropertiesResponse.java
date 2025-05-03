package org.sopt.makers.internal.member.dto.response;

import org.sopt.makers.internal.coffeechat.domain.enums.CoffeeChatStatus;

import java.util.List;

public record MemberPropertiesResponse(

        Long id,
        String major,
        String job,
        String organization,
        List<String> part,
        List<Integer> generation,
        CoffeeChatStatus coffeeChatStatus,
        Long receivedCoffeeChatCount,
        Long sentCoffeeChatCount,
        Long uploadSopticleCount,
        Long uploadReviewCount
) {
}
