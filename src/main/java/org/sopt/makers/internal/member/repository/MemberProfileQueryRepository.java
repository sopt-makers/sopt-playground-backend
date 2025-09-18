package org.sopt.makers.internal.member.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.coffeechat.domain.QCoffeeChat;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.QMember;
import org.sopt.makers.internal.member.dto.MemberProfileProjectDao;
import org.sopt.makers.internal.member.dto.QMemberProfileProjectDao;
import org.sopt.makers.internal.project.domain.QMemberProjectRelation;
import org.sopt.makers.internal.project.domain.QProject;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberProfileQueryRepository {

    private final JPAQueryFactory queryFactory;

    private BooleanExpression checkMemberHasProfile() {
        return QMember.member.hasProfile.eq(true);
    }

    private BooleanExpression checkMemberMbti(String mbti) {
        boolean isMbtiEmpty = !StringUtils.hasText(mbti);
        return isMbtiEmpty ? null : QMember.member.mbti.eq(mbti);
    }

    public List<MemberProfileProjectDao> findMemberProfileProjectsByMemberId (Long memberId) {
        val project = QProject.project;
        val relation = QMemberProjectRelation.memberProjectRelation;
        return queryFactory.select(
                        new QMemberProfileProjectDao(
                                project.id, project.writerId, project.name, project.summary, project.generation,
                                project.category, project.logoImage, project.thumbnailImage, project.serviceType
                        )).from(project)
                .innerJoin(relation).on(project.id.eq(relation.projectId))
                .where(relation.isTeamMember.isTrue().and(relation.userId.eq(memberId)))
                .fetch();
    }

    public List<Member> findAllMembersByCoffeeChatActivate() {
        QMember member = QMember.member;
        QCoffeeChat coffeeChat = QCoffeeChat.coffeeChat;

        return queryFactory
                .selectFrom(member)
                .innerJoin(coffeeChat).on(coffeeChat.member.eq(member))
                .where(coffeeChat.isCoffeeChatActivate.isTrue())
                .fetch();
    }

    public List<Long> findAllMemberIdsByRecommendFilter(String university, String mbti) {
        QMember member = QMember.member;

        return queryFactory
                .selectDistinct(member.id)
                .from(member)
                .where(checkMemberHasProfile(),
                        checkMemberMbti(mbti),
                        checkMemberUniversity(university))
                .fetch();
    }

    private BooleanExpression checkMemberUniversity(String university) {
        boolean isUniversityEmpty = !StringUtils.hasText(university);
        return isUniversityEmpty ? null : QMember.member.university.contains(university);
    }

}
