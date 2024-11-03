package org.sopt.makers.internal.member.repository.coffeechat.dto;

import org.sopt.makers.internal.member.domain.coffeechat.Career;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatTopicType;

import java.time.LocalDateTime;
import java.util.List;

public record CoffeeChatInfoDto(

        Long memberId,

        String bio,

        List<CoffeeChatTopicType> topicTypeList,

        String profileImage,

        String name,

        Career career,

        String university,

        LocalDateTime createdAt,

        Boolean isMine,

        Boolean isBlind
) {
}
