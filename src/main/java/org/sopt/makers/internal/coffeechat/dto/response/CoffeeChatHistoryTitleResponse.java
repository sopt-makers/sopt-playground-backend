package org.sopt.makers.internal.coffeechat.dto.response;

import org.sopt.makers.internal.coffeechat.domain.enums.Career;
import org.sopt.makers.internal.coffeechat.domain.enums.CoffeeChatTopicType;

import java.util.List;

public record CoffeeChatHistoryTitleResponse(

        List<CoffeeChatHistoryResponse> coffeeChatHistories
) {

    public record CoffeeChatHistoryResponse(

            Long id,

            String coffeeChatBio,

            String name,

            Career career,

            List<CoffeeChatTopicType> coffeeChatTopicType
    ) {}
}
