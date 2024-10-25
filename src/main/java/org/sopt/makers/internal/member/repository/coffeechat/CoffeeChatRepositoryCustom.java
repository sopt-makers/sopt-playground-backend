package org.sopt.makers.internal.member.repository.coffeechat;

import org.sopt.makers.internal.member.domain.coffeechat.Career;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatSection;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatTopicType;
import org.sopt.makers.internal.member.repository.coffeechat.dto.CoffeeChatInfoDto;

import java.util.List;

public interface CoffeeChatRepositoryCustom {

    List<CoffeeChatInfoDto> findRecentCoffeeChatInfo();
    List<CoffeeChatInfoDto> findSearchCoffeeChatInfo(CoffeeChatSection section, CoffeeChatTopicType topicType, Career career, String part, String search);
}
