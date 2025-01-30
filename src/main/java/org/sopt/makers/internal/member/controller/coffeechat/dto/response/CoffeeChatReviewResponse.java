package org.sopt.makers.internal.member.controller.coffeechat.dto.response;

import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatTopicType;

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
