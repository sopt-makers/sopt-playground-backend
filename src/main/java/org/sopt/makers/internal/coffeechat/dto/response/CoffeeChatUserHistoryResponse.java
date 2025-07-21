package org.sopt.makers.internal.coffeechat.dto.response;

import java.util.List;
import org.sopt.makers.internal.coffeechat.domain.enums.Career;
import org.sopt.makers.internal.coffeechat.domain.enums.CoffeeChatTopicType;

public record CoffeeChatUserHistoryResponse(

        Long id,

        String coffeeChatBio,

        String name,

        Career career,

        List<CoffeeChatTopicType> coffeeChatTopicType
) {}
