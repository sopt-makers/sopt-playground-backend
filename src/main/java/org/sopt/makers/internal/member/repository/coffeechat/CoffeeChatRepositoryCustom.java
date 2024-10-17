package org.sopt.makers.internal.member.repository.coffeechat;

import org.sopt.makers.internal.member.repository.coffeechat.dto.CoffeeChatInfoDto;

import java.util.List;

public interface CoffeeChatRepositoryCustom {

    List<CoffeeChatInfoDto> findRecentCoffeeChatInfo();
}
