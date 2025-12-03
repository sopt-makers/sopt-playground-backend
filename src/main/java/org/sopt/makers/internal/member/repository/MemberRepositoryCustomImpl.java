package org.sopt.makers.internal.member.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.QMember;

import java.util.List;

@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findTlMemberIdsByGenerationRandomly(Integer generation) {
        QMember member = QMember.member;

        return queryFactory
                .select(member.id)
                .from(member)
                .where(member.tlGeneration.eq(generation))
                .orderBy(Expressions.numberTemplate(Double.class, "function('random')").asc())
                .fetch();
    }
}
