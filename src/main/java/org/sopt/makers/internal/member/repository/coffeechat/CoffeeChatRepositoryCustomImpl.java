package org.sopt.makers.internal.member.repository.coffeechat;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.coffeechat.QCoffeeChatHistory;

import java.util.List;

@RequiredArgsConstructor
public class CoffeeChatRepositoryCustomImpl implements CoffeeChatRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findRecentCoffeeChatMember() {

        QCoffeeChatHistory coffeeChatHistory = QCoffeeChatHistory.coffeeChatHistory;

        return queryFactory
                .select(coffeeChatHistory.receiver.id)
                .distinct()
                .from(coffeeChatHistory)
                .orderBy(coffeeChatHistory.createdAt.desc())
                .limit(6)
                .fetch();
    }
}
