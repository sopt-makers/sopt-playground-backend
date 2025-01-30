package org.sopt.makers.internal.member.repository.coffeechat;

import org.sopt.makers.internal.member.controller.coffeechat.dto.response.CoffeeChatHistoryTitleResponse.CoffeeChatHistoryResponse;
import org.sopt.makers.internal.member.domain.coffeechat.Career;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatSection;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatTopicType;
import org.sopt.makers.internal.member.repository.coffeechat.dto.CoffeeChatInfoDto;
import org.sopt.makers.internal.member.repository.coffeechat.dto.RecentCoffeeChatInfoDto;

import java.util.List;

public interface CoffeeChatRepositoryCustom {

    List<RecentCoffeeChatInfoDto> findRecentCoffeeChatInfo();
    List<CoffeeChatInfoDto> findSearchCoffeeChatInfo(Long memberId, CoffeeChatSection section, CoffeeChatTopicType topicType, Career career, String part, String search);
    List<CoffeeChatHistoryResponse> getCoffeeChatHistoryTitles(Long memberId);
}
