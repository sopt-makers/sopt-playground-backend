package org.sopt.makers.internal.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.QMakersTeam;
import org.sopt.makers.internal.domain.QMember;
import org.sopt.makers.internal.domain.QMemberMakersActivity;
import org.sopt.makers.internal.dto.member.MakersMemberActivityDao;
import org.sopt.makers.internal.dto.member.QMakersMemberActivityDao;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MakersMemberQueryRepository {

  private final JPAQueryFactory queryFactory;

  public List<MakersMemberActivityDao> findAllMakersTeam() {
    val member = QMember.member;
    val mt = QMakersTeam.makersTeam;
    val mma = QMemberMakersActivity.memberMakersActivity;

    val makersActivities = queryFactory.select(
        new QMakersMemberActivityDao(
            mma.id, mma.memberId, mma.teamId, mma.part, mma.generation,
            mt.name, mt.description
        ))
        .from(mma)
        .innerJoin(mt)
        .on(mma.teamId.eq(mt.id))
        .fetch();
    val makersMemberIds = makersActivities.stream()
        .map(MakersMemberActivityDao::memberId)
        .distinct()
        .collect(Collectors.toList());

    val makersMembers = queryFactory.selectFrom(member)
        .where(member.id.in(makersMemberIds))
        .fetch();



}
