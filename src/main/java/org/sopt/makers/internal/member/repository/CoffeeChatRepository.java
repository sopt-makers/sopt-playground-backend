package org.sopt.makers.internal.member.repository;

import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoffeeChatRepository extends JpaRepository<CoffeeChat, Long> {
}
