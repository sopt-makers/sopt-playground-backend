package org.sopt.makers.internal.coffeechat.repository;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.coffeechat.domain.CoffeeChat;
import org.sopt.makers.internal.coffeechat.domain.CoffeeChatReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoffeeChatReviewRepository extends JpaRepository<CoffeeChatReview, Long> {

    List<CoffeeChatReview> findTop4ByOrderByIdDesc();
    List<CoffeeChatReview> findTop6ByOrderByIdDesc();
    Boolean existsByReviewerAndCoffeeChat(Member member, CoffeeChat coffeeChat);
}
