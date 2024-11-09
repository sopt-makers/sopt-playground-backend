package org.sopt.makers.internal.member.service.coffeechat.dto;

import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatStatus;

public record MemberCoffeeChatPropertyDto(

        CoffeeChatStatus coffeeChatStatus,

        Long receivedCoffeeChatCount,

        Long sentCoffeeChatCount
) {
}
