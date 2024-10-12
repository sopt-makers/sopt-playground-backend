package org.sopt.makers.internal.member.repository.career;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.domain.QMemberCareer;

import java.util.Optional;

@RequiredArgsConstructor
public class MemberCareerRepositoryCustomImpl implements MemberCareerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<MemberCareer> findMemberLastCareerByMemberId(Long memberId) {

        QMemberCareer memberCareer = QMemberCareer.memberCareer;

        return queryFactory
                .selectFrom(memberCareer)
                .where(memberCareer.memberId.eq(memberId))
                .orderBy(memberCareer.startDate.desc())
                .stream().findFirst();
    }
}
