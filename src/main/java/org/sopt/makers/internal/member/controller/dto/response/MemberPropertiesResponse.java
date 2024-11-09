package org.sopt.makers.internal.member.controller.dto.response;

import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatStatus;

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

        Long sentCoffeeChatCount
) {
}
