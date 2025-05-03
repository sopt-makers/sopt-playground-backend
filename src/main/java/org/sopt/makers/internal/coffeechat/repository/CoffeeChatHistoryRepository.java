package org.sopt.makers.internal.coffeechat.repository;

import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.coffeechat.domain.CoffeeChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoffeeChatHistoryRepository extends JpaRepository<CoffeeChatHistory, Long> {

    // CREATE

    // READ
    Long countBySender(Member sender);
    Long countByReceiver(Member receiver);
    Boolean existsByReceiverAndSender(Member receiver, Member sender);

    // UPDATE

    // DELETE
}
