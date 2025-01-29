package org.sopt.makers.internal.member.controller.coffeechat.dto.response;

import org.sopt.makers.internal.member.domain.coffeechat.Career;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatTopicType;

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
