package org.sopt.makers.internal.coffeechat.dto.response;

import java.util.List;

public record CoffeeChatResponse(

        List<CoffeeChatVo> coffeeChatList
) {
    public record CoffeeChatVo(

            Long memberId,

            String bio,

            List<String> topicTypeList,

            String profileImage,

            String name,

            String career,

            String organization,

            String companyJob,

            List<String> soptActivities,

            Boolean isMine,

            Boolean isBlind
    ) {}
}
