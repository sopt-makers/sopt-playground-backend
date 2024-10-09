package org.sopt.makers.internal.member.repository.coffeechat;

import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoffeeChatRepository extends JpaRepository<CoffeeChat, Long> {

    // CREATE

    // READ
    List<CoffeeChat> findAllByIsCoffeeChatActivate(boolean isCoffeeChatActivate);

    // UPDATE

    // DELETE
}
