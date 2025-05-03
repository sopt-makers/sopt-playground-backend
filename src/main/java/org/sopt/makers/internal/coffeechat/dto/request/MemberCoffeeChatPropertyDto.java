package org.sopt.makers.internal.coffeechat.dto.request;

import org.sopt.makers.internal.coffeechat.domain.enums.CoffeeChatStatus;

public record MemberCoffeeChatPropertyDto(

        CoffeeChatStatus coffeeChatStatus,

        Long receivedCoffeeChatCount,

        Long sentCoffeeChatCount
) {
}
