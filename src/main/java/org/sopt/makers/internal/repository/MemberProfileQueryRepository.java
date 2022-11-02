package org.sopt.makers.internal.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.QMember;
import org.sopt.makers.internal.domain.QMemberProjectRelation;
import org.sopt.makers.internal.domain.QProject;
import org.sopt.makers.internal.dto.member.MemberProfileProjectDao;
import org.sopt.makers.internal.dto.member.QMemberProfileProjectDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberProfileQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<MemberProfileProjectDao> findMemberProfileProjectsByMemberId (Long memberId) {
        val project = QProject.project;
        val relation = QMemberProjectRelation.memberProjectRelation;
        val member = QMember.member;
        return queryFactory.select(
                new QMemberProfileProjectDao(
                        project.id, project.writerId, project.name, project.summary, project.generation,
                        project.category, project.logoImage, project.thumbnailImage
                )).from(project)
                .innerJoin(relation).on(project.id.eq(relation.projectId))
                .where(relation.isTeamMember.and(relation.userId.eq(memberId)))
                .fetch();
    }


}
