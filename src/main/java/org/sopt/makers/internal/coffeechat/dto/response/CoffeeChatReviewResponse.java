package org.sopt.makers.internal.coffeechat.dto.response;

import org.sopt.makers.internal.coffeechat.domain.enums.CoffeeChatTopicType;

import java.util.List;

public record CoffeeChatReviewResponse(

        List<CoffeeChatReviewInfo> coffeeChatReviewList
) {

    public record CoffeeChatReviewInfo(

            String profileImage,

            String nickname,

            List<String> soptActivities,

            List<CoffeeChatTopicType> coffeeChatTopicType,

            String content
    ) { }
}
