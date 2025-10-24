package org.sopt.makers.internal.coffeechat.dto.request;

import org.sopt.makers.internal.coffeechat.domain.enums.Career;
import org.sopt.makers.internal.coffeechat.domain.enums.CoffeeChatTopicType;
import org.springframework.aot.hint.annotation.Reflective;

import java.time.LocalDateTime;
import java.util.List;

@Reflective
public record RecentCoffeeChatInfoDto(

        Long memberId,

        String bio,

        List<CoffeeChatTopicType> topicTypeList,

        Career career,

        String university,

        LocalDateTime createdAt
) {
}
