package org.sopt.makers.internal.coffeechat.dto.response;

import java.util.List;
import org.sopt.makers.internal.coffeechat.domain.enums.Career;
import org.sopt.makers.internal.coffeechat.domain.enums.CoffeeChatTopicType;

public record CoffeeChatHistoryResponse(

        Long id,

        String coffeeChatBio,

        Long memberId,

        Career career,

        List<CoffeeChatTopicType> coffeeChatTopicType
) {}
