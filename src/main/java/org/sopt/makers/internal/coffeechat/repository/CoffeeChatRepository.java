package org.sopt.makers.internal.coffeechat.repository;

import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.coffeechat.domain.CoffeeChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoffeeChatRepository extends JpaRepository<CoffeeChat, Long>, CoffeeChatRepositoryCustom {

    // CREATE

    // READ
    Boolean existsCoffeeChatByMember(Member member);
    Optional<CoffeeChat> findCoffeeChatByMember(Member member);

    // UPDATE

    // DELETE
}
