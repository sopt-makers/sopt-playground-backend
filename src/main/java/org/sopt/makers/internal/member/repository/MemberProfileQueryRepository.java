package org.sopt.makers.internal.member.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.coffeechat.domain.QCoffeeChat;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.QMember;
import org.sopt.makers.internal.member.domain.QMemberCareer;
import org.sopt.makers.internal.member.dto.MemberProfileProjectDao;
import org.sopt.makers.internal.project.domain.QMemberProjectRelation;
import org.sopt.makers.internal.project.domain.QProject;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberProfileQueryRepository {

    private final JPAQueryFactory queryFactory;

    private final QMember member = QMember.member;
    private final QMemberCareer memberCareer = QMemberCareer.memberCareer;
    private final QProject project = QProject.project;
    private final QMemberProjectRelation relation = QMemberProjectRelation.memberProjectRelation;
    private final QCoffeeChat coffeeChat = QCoffeeChat.coffeeChat;

    private BooleanExpression checkMemberHasProfile() {
        return member.hasProfile.eq(true);
    }

    private BooleanExpression checkMemberMbti(String mbti) {
        Boolean isMbtiEmpty = !StringUtils.hasText(mbti);
        return isMbtiEmpty ? null : member.mbti.eq(mbti);
    }

    public List<MemberProfileProjectDao> findMemberProfileProjectsByMemberId (Long memberId) {
        return queryFactory.select(
                        Projections.constructor(MemberProfileProjectDao.class,
                                project.id, project.writerId, project.name, project.summary, project.generation,
                                project.category, project.logoImage, project.thumbnailImage, project.serviceType
                        )).from(project)
                .innerJoin(relation).on(project.id.eq(relation.projectId))
                .where(relation.isTeamMember.isTrue().and(relation.userId.eq(memberId)))
                .fetch();
    }

    public List<Member> findAllMembersByCoffeeChatActivate() {
        return queryFactory
                .selectFrom(member)
                .innerJoin(coffeeChat).on(coffeeChat.member.eq(member))
                .where(coffeeChat.isCoffeeChatActivate.isTrue())
                .fetch();
    }

    public List<Long> findAllMemberIdsByRecommendFilter(String university, String mbti) {
        return queryFactory.select(member.id)
            .from(member)
            .where(checkMemberHasProfile(),
                    checkMemberMbti(mbti),
                    checkMemberUniversity(university))
            .groupBy(member.id)
            .fetch();
    }

    private BooleanExpression checkMemberUniversity(String university) {
        Boolean isUniversityEmpty = !StringUtils.hasText(university);
        return isUniversityEmpty ? null : member.university.contains(university);
    }

    // 서버 측 전체 ID 조회: limit/cursor 없이 DB 필터만 적용하여 모든 userId 반환
    public List<Long> findAllMemberIdsByDbFilters(String mbti, Integer employed, String search) {
        return queryFactory
                .selectDistinct(member.id)
                .from(member)
                .leftJoin(member.careers, memberCareer)
                .where(
                        checkMemberHasProfile(),
                        checkMemberMbti(mbti),
                        checkEmployed(employed)
                        // checkSearchUniversityOrCompany(search)
                )
                .fetch();
    }

    // TODO. 현재 이름 검색과 겹치므로 조건에서 제외
    private BooleanExpression checkSearchUniversityOrCompany(String search) {
        Boolean isEmpty = !StringUtils.hasText(search);
        if (isEmpty) return null;
        return member.university.contains(search)
                .or(memberCareer.companyName.contains(search));
    }

    private BooleanExpression checkEmployed(Integer employed) {
        if (employed == null) return null;
        if (employed == 1) {
            return JPAExpressions.selectOne()
                    .from(memberCareer)
                    .where(memberCareer.memberId.eq(member.id)
                            .and(memberCareer.isCurrent.isTrue()))
                    .exists();
        }
        if (employed == 0) {
            return JPAExpressions.selectOne()
                    .from(memberCareer)
                    .where(memberCareer.memberId.eq(member.id)
                            .and(memberCareer.isCurrent.isTrue()))
                    .notExists();
        }
        return null;
    }

}
