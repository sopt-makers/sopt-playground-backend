package org.sopt.makers.internal.member.controller.coffeechat.dto.response;

import java.util.List;

public record CoffeeChatResponse(

        List<CoffeeChatVo> recentCoffeeChatList
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

            List<String> soptActivities
    ) {}
}
