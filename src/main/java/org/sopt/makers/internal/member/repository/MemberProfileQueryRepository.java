package org.sopt.makers.internal.member.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.coffeechat.domain.QCoffeeChat;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.QMember;
import org.sopt.makers.internal.member.domain.QMemberCareer;
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
        val isMbtiEmpty = !StringUtils.hasText(mbti);
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
        val member = QMember.member;

        return queryFactory.select(member.id)
            .from(member)
            .where(checkMemberHasProfile(),
                    checkMemberMbti(mbti),
                    checkMemberUniversity(university))
            .groupBy(member.id)
            .fetch();
    }

    private BooleanExpression checkMemberUniversity(String university) {
        val isUniversityEmpty = !StringUtils.hasText(university);
        return isUniversityEmpty ? null : QMember.member.university.contains(university);
    }

    // 서버 측 전체 ID 조회: limit/cursor 없이 DB 필터만 적용하여 모든 userId 반환
    public List<Long> findAllMemberIdsByDbFilters(String mbti, Integer employed, String search) {
        val member = QMember.member;
        val career = QMemberCareer.memberCareer;

        return queryFactory
                .selectDistinct(member.id)
                .from(member)
                .leftJoin(member.careers, career)
                .where(
                        checkMemberHasProfile(),
                        checkMemberMbti(mbti),
                        checkEmployed(employed),
                        checkSearchUniversityOrCompany(search)
                )
                .orderBy(member.id.desc())
                .fetch();
    }

    private BooleanExpression checkSearchUniversityOrCompany(String search) {
        val isEmpty = !StringUtils.hasText(search);
        if (isEmpty) return null;
        val member = QMember.member;
        val career = QMemberCareer.memberCareer;
        return member.university.contains(search)
                .or(career.companyName.contains(search));
    }

    private BooleanExpression checkEmployed(Integer employed) {
        if (employed == null) return null;
        val member = QMember.member;
        if (employed == 1) {
            return JPAExpressions.selectOne()
                    .from(QMemberCareer.memberCareer)
                    .where(QMemberCareer.memberCareer.memberId.eq(member.id)
                            .and(QMemberCareer.memberCareer.isCurrent.isTrue()))
                    .exists();
        }
        if (employed == 0) {
            return JPAExpressions.selectOne()
                    .from(QMemberCareer.memberCareer)
                    .where(QMemberCareer.memberCareer.memberId.eq(member.id)
                            .and(QMemberCareer.memberCareer.isCurrent.isTrue()))
                    .notExists();
        }
        return null;
    }

}
