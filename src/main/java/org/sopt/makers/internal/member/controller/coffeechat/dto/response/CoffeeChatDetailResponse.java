package org.sopt.makers.internal.member.controller.coffeechat.dto.response;

import java.util.List;

public record CoffeeChatDetailResponse(

        String bio,

        Long memberId,

        String profileImage,

        String name,

        String career,

        String organization,

        String memberCareerTitle,

        String phone,

        String email,

        String introduction,

        List<String> sections,

        List<String> topicTypeList,

        String topic,

        String meetingType,

        String guideline,

        Boolean isCoffeeChatActivate,

        Boolean isMine,

        Boolean isBlind
) {
}
