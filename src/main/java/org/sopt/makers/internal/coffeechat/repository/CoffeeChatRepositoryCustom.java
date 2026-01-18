package org.sopt.makers.internal.coffeechat.repository;

import java.util.List;
import org.sopt.makers.internal.coffeechat.domain.enums.Career;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatInfoDto;
import org.sopt.makers.internal.coffeechat.dto.request.RecentCoffeeChatInfoDto;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatHistoryResponse;

public interface CoffeeChatRepositoryCustom {

    List<RecentCoffeeChatInfoDto> findRecentCoffeeChatInfo();
    List<CoffeeChatInfoDto> findCoffeeChatInfoByDbConditions(Long memberId, Career career);
    List<CoffeeChatHistoryResponse> getCoffeeChatHistoryTitles(Long memberId);
}
