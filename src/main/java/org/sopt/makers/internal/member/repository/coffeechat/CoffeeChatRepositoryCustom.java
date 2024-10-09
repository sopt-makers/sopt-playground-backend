package org.sopt.makers.internal.member.repository.coffeechat;

import java.util.List;

public interface CoffeeChatRepositoryCustom {

    List<Long> findMemberIdsByIsCoffeeChatActivate(boolean isCoffeeChatActivate);
}
