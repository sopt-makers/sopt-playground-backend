package org.sopt.makers.internal.member.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.coffeechat.domain.QCoffeeChat;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.QMember;
import org.sopt.makers.internal.member.domain.QMemberCareer;
import org.sopt.makers.internal.member.domain.QMemberLink;
import org.sopt.makers.internal.member.dto.MemberProfileProjectDao;
import org.sopt.makers.internal.member.dto.profile.MemberActivityCountVo;
import org.sopt.makers.internal.member.dto.profile.MemberBasicInfoVo;
import org.sopt.makers.internal.member.dto.profile.MemberFavorVo;
import org.sopt.makers.internal.member.dto.profile.MemberIntroVo;
import org.sopt.makers.internal.member.dto.profile.MemberPersonalityVo;
import org.sopt.makers.internal.member.dto.profile.MemberProfileSummaryVo;
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
    private final QMemberLink memberLink = QMemberLink.memberLink;

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

    /**
     * 프로필 목록 조회의 <b>선별·정렬</b>에 필요한 값만 조회한다. (엔티티를 로드하지 않는다)
     *
     * <p>기존에는 {@code Member} 엔티티를 전량 로드한 뒤 가중치 계산 과정에서 LAZY 컬렉션
     * ({@code links}, {@code careers})을 건드려 멤버당 2회의 추가 쿼리가 발생했다.
     * 여기서는 아래 3개 쿼리로 필요한 값을 한 번에 확보한다.
     *
     * <ol>
     *   <li>스칼라 필드 (중첩 Projection — SQL 은 단일 flat SELECT 로 나간다)</li>
     *   <li>링크 개수 (GROUP BY 집계)</li>
     *   <li>커리어 개수 + 회사명 (검색어 매칭에 회사명 값이 필요해 행 단위로 가져온다)</li>
     * </ol>
     */
    public List<MemberProfileSummaryVo> findMemberProfileSummariesByIds(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return List.of();
        }

        List<MemberProfileSummaryVo> profileSummaries = queryFactory
                .select(Projections.constructor(MemberProfileSummaryVo.class,
                        member.id,
                        Projections.constructor(MemberBasicInfoVo.class,
                                member.address, member.university, member.major),
                        Projections.constructor(MemberIntroVo.class,
                                member.introduction, member.selfIntroduction, member.skill),
                        Projections.constructor(MemberPersonalityVo.class,
                                member.mbti, member.mbtiDescription, member.sojuCapacity,
                                member.interest, member.idealType),
                        Projections.constructor(MemberFavorVo.class,
                                member.userFavor.isPourSauceLover,
                                member.userFavor.isHardPeachLover,
                                member.userFavor.isMintChocoLover,
                                member.userFavor.isRedBeanFishBreadLover,
                                member.userFavor.isSojuLover,
                                member.userFavor.isRiceTteokLover)))
                .from(member)
                .where(member.id.in(memberIds))
                .fetch();

        Map<Long, Integer> linkCountMap = findLinkCountMap(memberIds);
        Map<Long, MemberActivityCountVo> activityMap = buildActivityMap(memberIds, linkCountMap);

        return profileSummaries.stream()
                .map(profileSummary -> profileSummary.withActivity(
                        activityMap.getOrDefault(
                                profileSummary.id(),
                                new MemberActivityCountVo(
                                        linkCountMap.getOrDefault(profileSummary.id(), 0), 0, List.of())
                        )))
                .toList();
    }

    private Map<Long, Integer> findLinkCountMap(List<Long> memberIds) {
        List<Tuple> rows = queryFactory
                .select(memberLink.memberId, memberLink.count())
                .from(memberLink)
                .where(memberLink.memberId.in(memberIds))
                .groupBy(memberLink.memberId)
                .fetch();

        Map<Long, Integer> linkCountMap = new HashMap<>();
        for (Tuple row : rows) {
            Long memberId = row.get(memberLink.memberId);
            Long count = row.get(memberLink.count());
            if (memberId != null) {
                linkCountMap.put(memberId, count == null ? 0 : count.intValue());
            }
        }
        return linkCountMap;
    }

    /**
     * 커리어는 개수(가중치)와 회사명(검색) 둘 다 필요해서 행 단위로 조회한 뒤 함께 집계한다.
     * company_name 이 NULL 인 커리어도 개수에는 포함되지만 검색 대상에서는 제외된다 (기존 로직과 동일).
     */
    private Map<Long, MemberActivityCountVo> buildActivityMap(
            List<Long> memberIds,
            Map<Long, Integer> linkCountMap
    ) {
        List<Tuple> rows = queryFactory
                .select(memberCareer.memberId, memberCareer.companyName)
                .from(memberCareer)
                .where(memberCareer.memberId.in(memberIds))
                .fetch();

        Map<Long, Integer> careerCountMap = new HashMap<>();
        Map<Long, List<String>> companyNameMap = new HashMap<>();
        for (Tuple row : rows) {
            Long memberId = row.get(memberCareer.memberId);
            if (memberId == null) {
                continue;
            }
            careerCountMap.merge(memberId, 1, Integer::sum);

            String companyName = row.get(memberCareer.companyName);
            if (companyName != null) {
                companyNameMap.computeIfAbsent(memberId, key -> new ArrayList<>()).add(companyName);
            }
        }

        Map<Long, MemberActivityCountVo> activityMap = new HashMap<>();
        for (Long memberId : careerCountMap.keySet()) {
            activityMap.put(memberId, new MemberActivityCountVo(
                    linkCountMap.getOrDefault(memberId, 0),
                    careerCountMap.getOrDefault(memberId, 0),
                    companyNameMap.getOrDefault(memberId, List.of())
            ));
        }
        return activityMap;
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
