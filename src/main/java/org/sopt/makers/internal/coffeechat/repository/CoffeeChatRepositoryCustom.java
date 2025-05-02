package org.sopt.makers.internal.coffeechat.repository;

import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatHistoryTitleResponse.CoffeeChatHistoryResponse;
import org.sopt.makers.internal.coffeechat.domain.enums.Career;
import org.sopt.makers.internal.coffeechat.domain.enums.CoffeeChatSection;
import org.sopt.makers.internal.coffeechat.domain.enums.CoffeeChatTopicType;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatInfoDto;
import org.sopt.makers.internal.coffeechat.dto.request.RecentCoffeeChatInfoDto;

import java.util.List;

public interface CoffeeChatRepositoryCustom {

    List<RecentCoffeeChatInfoDto> findRecentCoffeeChatInfo();
    List<CoffeeChatInfoDto> findSearchCoffeeChatInfo(Long memberId, CoffeeChatSection section, CoffeeChatTopicType topicType, Career career, String part, String search);
    List<CoffeeChatHistoryResponse> getCoffeeChatHistoryTitles(Long memberId);
}
