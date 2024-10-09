package org.sopt.makers.internal.member.repository.coffeechat;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.QCoffeeChat;

import java.util.List;

@RequiredArgsConstructor
public class CoffeeChatRepositoryCustomImpl implements CoffeeChatRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findMemberIdsByIsCoffeeChatActivate(boolean isCoffeeChatActivate) {

        QCoffeeChat coffeeChat = QCoffeeChat.coffeeChat;

        return queryFactory
                .select(coffeeChat.member.id)
                .from(coffeeChat)
                .where(coffeeChat.isCoffeeChatActivate.eq(isCoffeeChatActivate))
                .fetch();
    }
}
