package org.sopt.makers.internal.member.controller.coffeechat.dto.response;

import java.util.List;

public record CoffeeChatDetailResponse(

        String bio,

        Long memberId,

        String name,

        String career,

        String organization,

        String companyJob,

        String phone,

        String email,

        String introduction,

        List<String> topicTypeList,

        String topic,

        String meetingType,

        String guideline,

        Boolean isMine
) {
}
