package org.sopt.makers.internal.member.repository.coffeechat;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.QMember;
import org.sopt.makers.internal.member.domain.coffeechat.QCoffeeChat;
import org.sopt.makers.internal.member.domain.coffeechat.QCoffeeChatHistory;
import org.sopt.makers.internal.member.repository.coffeechat.dto.CoffeeChatInfoDto;

import java.util.List;

@RequiredArgsConstructor
public class CoffeeChatRepositoryCustomImpl implements CoffeeChatRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CoffeeChatInfoDto> findRecentCoffeeChatInfo() {

        QCoffeeChat coffeeChat = QCoffeeChat.coffeeChat;
        QCoffeeChatHistory coffeeChatHistory = QCoffeeChatHistory.coffeeChatHistory;
        QMember member = QMember.member;

        return queryFactory
                .select(Projections.constructor(
                        CoffeeChatInfoDto.class,
                        coffeeChat.member.id,
                        coffeeChat.coffeeChatBio,
                        coffeeChat.coffeeChatTopicType,
                        member.profileImage,
                        member.name,
                        coffeeChat.career,
                        member.university,
                        coffeeChatHistory.createdAt
                ))
                .from(coffeeChat)
                .leftJoin(member).on(coffeeChat.member.id.eq(member.id))
                .join(coffeeChatHistory).on(coffeeChat.member.id.eq(coffeeChatHistory.receiver.id))
                .orderBy(coffeeChatHistory.createdAt.desc())
                .limit(6)
                .fetch();
    }
}
