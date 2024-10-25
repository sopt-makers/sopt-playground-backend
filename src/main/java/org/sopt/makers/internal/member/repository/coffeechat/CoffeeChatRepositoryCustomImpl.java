package org.sopt.makers.internal.member.repository.coffeechat;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.QMember;
import org.sopt.makers.internal.domain.QMemberCareer;
import org.sopt.makers.internal.domain.QMemberSoptActivity;
import org.sopt.makers.internal.member.domain.coffeechat.*;
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

    @Override
    public List<CoffeeChatInfoDto> findSearchCoffeeChatInfo(Long memberId, CoffeeChatSection section, CoffeeChatTopicType topicType, Career career, String part, String search) {

        QCoffeeChat coffeeChat = QCoffeeChat.coffeeChat;
        QMember member = QMember.member;
        QMemberCareer memberCareer = QMemberCareer.memberCareer;
        QMemberSoptActivity memberSoptActivity = QMemberSoptActivity.memberSoptActivity;

        // 검색 조건
        BooleanBuilder builder = new BooleanBuilder();
        StringPath sectionStringPath = Expressions.stringPath("section");
        StringPath topicTypeStringPath = Expressions.stringPath("coffee_chat_topic_type");
        if (section != null) {
            builder.and(sectionStringPath.contains(section.name()));
        }
        if (topicType != null) {
            builder.and(topicTypeStringPath.contains(topicType.name()));
        }
        builder.and(isInCareer(coffeeChat, career));
        if (search != null && !search.isEmpty()) {
            builder.or(memberCareer.companyName.like("%" + search + "%"))
                    .or(member.university.like("%" + search + "%"))
                    .or(member.name.like("%" + search + "%"));
        }
        if (part != null) {
            builder.and(JPAExpressions
                    .selectFrom(memberSoptActivity)
                    .where(memberSoptActivity.memberId.eq(member.id)
                            .and(memberSoptActivity.part.like(part + "%"))
                    )
                    .exists()
            );
        }
        builder.or(coffeeChat.isCoffeeChatActivate.isTrue())
                .or(coffeeChat.member.id.eq(memberId));

        return queryFactory
                .select(Projections.constructor(
                        CoffeeChatInfoDto.class,
                        coffeeChat.member.id,
                        coffeeChat.coffeeChatBio,
                        coffeeChat.coffeeChatTopicType,
                        coffeeChat.coffeeChatBio,
                        coffeeChat.coffeeChatBio,
                        coffeeChat.career,
                        coffeeChat.coffeeChatBio,
                        coffeeChat.createdAt
                ))
                .from(coffeeChat)
                .leftJoin(member).on(coffeeChat.member.id.eq(member.id))
                .leftJoin(memberCareer).on(coffeeChat.member.id.eq(memberCareer.memberId))
                .where(builder)
                .fetch();
    }

    private BooleanExpression isInCareer(QCoffeeChat coffeeChat, Career career) {

        if (career == null) {
            return null;
        }

        return coffeeChat.career.eq(career);
    }
}
