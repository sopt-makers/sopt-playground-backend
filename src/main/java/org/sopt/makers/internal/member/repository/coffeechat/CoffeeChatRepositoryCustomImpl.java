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
import org.sopt.makers.internal.member.repository.coffeechat.dto.RecentCoffeeChatInfoDto;

import java.util.List;

@RequiredArgsConstructor
public class CoffeeChatRepositoryCustomImpl implements CoffeeChatRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<RecentCoffeeChatInfoDto> findRecentCoffeeChatInfo() {

        QCoffeeChat coffeeChat = QCoffeeChat.coffeeChat;
        QCoffeeChatHistory coffeeChatHistory = QCoffeeChatHistory.coffeeChatHistory;
        QMember member = QMember.member;

        return queryFactory
                .select(Projections.constructor(
                        RecentCoffeeChatInfoDto.class,
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
        builder.and(isInSection(section))
                .and(isInTopicType(topicType))
                .and(isInCareer(coffeeChat, career))
                .and(isInSearch(member, memberCareer, search))
                .and(isInPart(member, part, memberSoptActivity))
                .and(coffeeChat.isCoffeeChatActivate.isTrue()
                        .or(coffeeChat.member.id.eq(memberId)));

        return queryFactory
                .selectDistinct(Projections.constructor(
                        CoffeeChatInfoDto.class,
                        coffeeChat.member.id,
                        coffeeChat.coffeeChatBio,
                        coffeeChat.coffeeChatTopicType,
                        coffeeChat.member.profileImage,
                        coffeeChat.member.name,
                        coffeeChat.career,
                        coffeeChat.member.university,
                        coffeeChat.createdAt,
                        coffeeChat.member.id.eq(memberId),
                        coffeeChat.isCoffeeChatActivate.isFalse()
                ))
                .from(coffeeChat)
                .leftJoin(member).on(coffeeChat.member.id.eq(member.id))
                .leftJoin(memberCareer).on(coffeeChat.member.id.eq(memberCareer.memberId))
                .where(builder)
                .fetch();
    }

    private BooleanExpression isInSection(CoffeeChatSection section) {
        if (section == null) {
            return null;
        }
        StringPath sectionStringPath = Expressions.stringPath("section");
        return sectionStringPath.contains(section.name());
    }

    private BooleanExpression isInTopicType(CoffeeChatTopicType topicType) {
        if (topicType == null) {
            return null;
        }
        StringPath topicTypeStringPath = Expressions.stringPath("coffee_chat_topic_type");
        return topicTypeStringPath.contains(topicType.name());
    }

    private BooleanExpression isInCareer(QCoffeeChat coffeeChat, Career career) {
        if (career == null) {
            return null;
        }
        return coffeeChat.career.eq(career);
    }

    private BooleanExpression isInSearch(QMember member, QMemberCareer memberCareer, String search) {
        if (search == null || search.isEmpty()) {
            return null;
        }
        return memberCareer.companyName.contains(search)
                .or(member.university.contains(search))
                .or(member.name.contains(search));
    }

    private BooleanExpression isInPart(QMember member, String part, QMemberSoptActivity memberSoptActivity) {
        if (part == null) {
            return null;
        }
        return JPAExpressions
                .selectFrom(memberSoptActivity)
                .where(memberSoptActivity.memberId.eq(member.id)
                        .and(memberSoptActivity.part.like(part + "%"))
                )
                .exists();
    }
}
