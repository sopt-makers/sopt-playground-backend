package org.sopt.makers.internal.member.repository.soptactivity;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.QMemberSoptActivity;
import org.sopt.makers.internal.member.repository.soptactivity.dto.SoptActivityInfoDto;

import java.util.List;

@RequiredArgsConstructor
public class MemberSoptActivityRepositoryCustomImpl implements MemberSoptActivityRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<SoptActivityInfoDto> findAllSoptActivitiesByMemberId(Long memberId) {

        QMemberSoptActivity memberSoptActivity = QMemberSoptActivity.memberSoptActivity;

        return queryFactory
                .select(Projections.constructor(
                        SoptActivityInfoDto.class,
                        memberSoptActivity.generation,
                        memberSoptActivity.part
                ))
                .from(memberSoptActivity)
                .where(memberSoptActivity.memberId.eq(memberId))
                .fetch();
    }
}
