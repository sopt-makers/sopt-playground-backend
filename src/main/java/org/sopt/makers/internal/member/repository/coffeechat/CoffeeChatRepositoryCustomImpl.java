package org.sopt.makers.internal.member.repository.coffeechat;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.QMember;
import org.sopt.makers.internal.domain.QMemberCareer;
import org.sopt.makers.internal.member.domain.coffeechat.QCoffeeChat;
import org.sopt.makers.internal.member.domain.coffeechat.QCoffeeChatHistory;
import org.sopt.makers.internal.member.repository.coffeechat.dto.CoffeeChatInfoDto;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CoffeeChatRepositoryCustomImpl implements CoffeeChatRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findRecentCoffeeChatMember() {
        QCoffeeChatHistory coffeeChatHistory = QCoffeeChatHistory.coffeeChatHistory;

        List<Tuple> queryData = queryFactory
                .select(coffeeChatHistory.receiver.id, coffeeChatHistory.createdAt)
                .from(coffeeChatHistory)
                .orderBy(coffeeChatHistory.createdAt.desc())
                .distinct()
                .limit(6)
                .fetch();

        return queryData.stream()
                .map(tuple -> tuple.get(coffeeChatHistory.receiver.id))
                .collect(Collectors.toList());
    }

    @Override
    public List<CoffeeChatInfoDto> findCoffeeChatInfoByMemberIdList(List<Long> memberIdList) {

        QCoffeeChat coffeeChat = QCoffeeChat.coffeeChat;
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
                        member.university
                ))
                .from(coffeeChat)
                .leftJoin(member).on(coffeeChat.member.id.eq(member.id))
                .where(coffeeChat.member.id.in(memberIdList))
                .fetch();
    }
}
