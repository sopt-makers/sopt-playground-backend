package org.sopt.makers.internal.member.repository.coffeechat;

import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoffeeChatHistoryRepository extends JpaRepository<CoffeeChatHistory, Long> {
}
