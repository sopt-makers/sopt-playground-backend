package org.sopt.makers.internal.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.QMember;
import org.sopt.makers.internal.domain.QMemberSoptActivity;
import org.sopt.makers.internal.domain.QSopticle;
import org.sopt.makers.internal.domain.QSopticleWriter;
import org.sopt.makers.internal.dto.sopticle.QSopticleDao;
import org.sopt.makers.internal.dto.sopticle.SopticleDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SopticleQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<SopticleDao> findById(Long id) {
        val sopticle = QSopticle.sopticle;
        val sopticleWriter = QSopticleWriter.sopticleWriter;
        val member = QMember.member;
        val memberSoptActivity = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.select(new QSopticleDao(
            sopticle.id, sopticle.link, sopticleWriter.writerId, member.name, member.profileImage,
                memberSoptActivity.part, memberSoptActivity.generation
        )).from(sopticle)
                .innerJoin(sopticleWriter).on(sopticle.id.eq(sopticleWriter.sopticleId))
                .innerJoin(member).on(sopticleWriter.writerId.eq(member.id))
                .innerJoin(memberSoptActivity).on(member.id.eq(memberSoptActivity.memberId))
                .where(sopticle.id.eq(id))
                .fetch();
    }
}
