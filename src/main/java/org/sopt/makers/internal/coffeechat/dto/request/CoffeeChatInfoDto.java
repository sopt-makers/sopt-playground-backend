package org.sopt.makers.internal.coffeechat.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.aot.hint.annotation.Reflective;
import org.sopt.makers.internal.coffeechat.domain.enums.Career;
import org.sopt.makers.internal.coffeechat.domain.enums.CoffeeChatSection;
import org.sopt.makers.internal.coffeechat.domain.enums.CoffeeChatTopicType;

@Reflective
public record CoffeeChatInfoDto(

        Long memberId,

        String bio,

        List<CoffeeChatSection> sectionList,

        List<CoffeeChatTopicType> topicTypeList,

        Career career,

        String university,

        LocalDateTime createdAt,

        Boolean isMine,

        Boolean isBlind,

        String companyName
) {
}
