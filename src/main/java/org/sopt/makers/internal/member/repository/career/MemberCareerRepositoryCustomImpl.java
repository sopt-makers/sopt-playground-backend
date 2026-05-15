package org.sopt.makers.internal.member.repository.career;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.MemberCareer;
import org.sopt.makers.internal.member.domain.QMemberCareer;

import java.util.List;
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

    @Override
    public List<MemberCareer> findMemberLastCareersByMemberIds(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return List.of();
        }

        QMemberCareer memberCareer = QMemberCareer.memberCareer;

        return queryFactory
            .selectFrom(memberCareer)
            .where(memberCareer.memberId.in(memberIds))
            .orderBy(
                memberCareer.memberId.asc(),
                memberCareer.startDate.desc(),
                memberCareer.id.desc()
            )
            .fetch();
    }
}
