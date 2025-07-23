package org.sopt.makers.internal.coffeechat.repository;

import java.util.Optional;
import org.sopt.makers.internal.coffeechat.domain.CoffeeChat;
import org.sopt.makers.internal.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoffeeChatRepository extends JpaRepository<CoffeeChat, Long>, CoffeeChatRepositoryCustom {

    // CREATE

    // READ
    Boolean existsCoffeeChatByMember(Member member);
    Optional<CoffeeChat> findCoffeeChatByMember(Member member);

    // UPDATE

    // DELETE
}
