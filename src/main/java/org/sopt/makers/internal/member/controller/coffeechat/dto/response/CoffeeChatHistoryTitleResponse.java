package org.sopt.makers.internal.member.controller.coffeechat.dto.response;

import java.util.List;

public record CoffeeChatHistoryTitleResponse(

        List<CoffeeChatHistoryResponse> coffeeChatHistories
) {

    public record CoffeeChatHistoryResponse(

            Long id,

            String title
    ) {}
}
