package org.sopt.makers.internal.coffeechat.dto.response;

import java.util.List;

public record CoffeeChatHistoryTitleResponse(

        List<CoffeeChatUserHistoryResponse> coffeeChatHistories
) {
}
