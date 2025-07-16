package org.sopt.makers.internal.coffeechat.dto.request;

import org.sopt.makers.internal.coffeechat.domain.enums.Career;
import org.sopt.makers.internal.coffeechat.domain.enums.CoffeeChatTopicType;

import java.time.LocalDateTime;
import java.util.List;

public record CoffeeChatInfoDto(

        Long memberId,

        String bio,

        List<CoffeeChatTopicType> topicTypeList,

        Career career,

        String university,

        LocalDateTime createdAt,

        Boolean isMine,

        Boolean isBlind
) {
}
