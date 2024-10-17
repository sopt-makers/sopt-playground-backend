package org.sopt.makers.internal.member.controller.coffeechat.dto.response;

import java.util.List;

public record RecentCoffeeChatResponse(

        List<RecentCoffeeChat> recentCoffeeChatList
) {
    public record RecentCoffeeChat(

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
