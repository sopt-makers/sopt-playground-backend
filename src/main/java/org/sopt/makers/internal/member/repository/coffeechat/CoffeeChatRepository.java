package org.sopt.makers.internal.member.repository.coffeechat;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoffeeChatRepository extends JpaRepository<CoffeeChat, Long>, CoffeeChatRepositoryCustom {

    // CREATE

    // READ
    Boolean existsCoffeeChatByMember(Member member);
    List<CoffeeChat> findAllByIsCoffeeChatActivate(boolean isCoffeeChatActivate);
    Optional<CoffeeChat> findCoffeeChatByMember(Member member);

    // UPDATE

    // DELETE
}
