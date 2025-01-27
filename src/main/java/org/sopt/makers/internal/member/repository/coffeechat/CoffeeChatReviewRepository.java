package org.sopt.makers.internal.member.repository.coffeechat;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChat;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoffeeChatReviewRepository extends JpaRepository<CoffeeChatReview, Long> {

    List<CoffeeChatReview> findTop4ByOrderByIdDesc();
    Boolean existsByReviewerAndCoffeeChat(Member member, CoffeeChat coffeeChat);
}
