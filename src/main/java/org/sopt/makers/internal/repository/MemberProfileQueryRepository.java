package org.sopt.makers.internal.repository;

import java.util.List;
import java.util.Objects;

import org.sopt.makers.internal.common.MakersMemberId;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.OrderByCondition;
import org.sopt.makers.internal.domain.Part;
import org.sopt.makers.internal.domain.QMember;
import org.sopt.makers.internal.domain.QMemberCareer;
import org.sopt.makers.internal.domain.QMemberProjectRelation;
import org.sopt.makers.internal.domain.QMemberSoptActivity;
import org.sopt.makers.internal.domain.QProject;
import org.sopt.makers.internal.dto.member.MemberProfileProjectDao;
import org.sopt.makers.internal.dto.member.QMemberProfileProjectDao;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Repository
@RequiredArgsConstructor
public class MemberProfileQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final List<Long> WHITE_LIST = List.of(894L);

    private BooleanExpression checkMemberContainsName(String name) {
        if(name == null) return null;
        return QMember.member.name.contains(name);
    }

    private BooleanExpression checkActivityContainsPart(String part) {
        if(part == null) return null;
        return QMemberSoptActivity.memberSoptActivity.part.contains(part);
    }

    private BooleanExpression checkUserContainsPart(String part) {
        if(part == null) return null;
        return QMember.member.id.in(queryFactory.select(QMember.member.id)
                .innerJoin(QMember.member.activities, QMemberSoptActivity.memberSoptActivity)
                .where(checkActivityContainsPart(part)));
    }

    private BooleanExpression checkActivityContainsGenerationAndTeam(Integer generation, String team) {
        if(generation == null && team == null) return null;
        return QMember.member.id.in(
                queryFactory.select(QMember.member.id)
                        .innerJoin(QMember.member.activities, QMemberSoptActivity.memberSoptActivity)
                        .where(QMemberSoptActivity.memberSoptActivity.generation.eq(generation)
                                        .and(checkActivityContainsTeam(team)))
        );
    }

    private BooleanExpression checkUserActivityContainsGeneration(Integer generation) {
        if(generation == null) return null;
        return QMember.member.id.in(
                queryFactory.select(QMember.member.id)
                        .innerJoin(QMember.member.activities, QMemberSoptActivity.memberSoptActivity)
                        .where(QMemberSoptActivity.memberSoptActivity.generation.eq(generation))
        );
    }

    private BooleanExpression checkActivityContainsGenerationAndPart(Integer generation, String part) {
        if(generation == null && part == null) return null;
        return QMember.member.id.in(
                queryFactory.select(QMember.member.id)
                        .innerJoin(QMember.member.activities, QMemberSoptActivity.memberSoptActivity)
                        .where(QMemberSoptActivity.memberSoptActivity.generation.eq(generation)
                                .and(checkActivityContainsPart(part)))
        );
    }

    private BooleanExpression checkActivityContainsPartAndTeam(String part, String team) {
        if(part == null && team == null) return null;
        else if(part == null) return checkActivityContainsTeam(team);
        else if(team == null) return checkActivityContainsPart(part);
        else {
            return QMember.member.id.in(
                    queryFactory.select(QMember.member.id)
                            .innerJoin(QMember.member.activities, QMemberSoptActivity.memberSoptActivity)
                            .where(checkActivityContainsTeam(team).and(checkUserContainsPart(part)))
            );
        }
    }

    private BooleanExpression checkActivityContainsGenerationAndTeamAndPart(Integer generation, String team, String part) {
        if(generation == null && team == null && part == null) return null;
        else if(generation == null) return checkActivityContainsPartAndTeam(part,team);
        else if(part == null) return checkActivityContainsGenerationAndTeam(generation,team);
        else if(team == null) return checkActivityContainsGenerationAndPart(generation,part);
        return QMember.member.id.in(
                queryFactory.select(QMember.member.id)
                        .innerJoin(QMember.member.activities, QMemberSoptActivity.memberSoptActivity)
                        .where(checkActivityContainsGenerationAndTeam(generation,team)
                                .and(checkActivityContainsGenerationAndPart(generation,part)))
        );
    }

    private BooleanExpression checkMemberHasProfile() {
        return QMember.member.hasProfile.eq(true);
    }

    private BooleanExpression checkMemberMbti(String mbti) {
        val isMbtiEmpty = !StringUtils.hasText(mbti);
        return isMbtiEmpty ? null : QMember.member.mbti.eq(mbti);
    }

    private BooleanExpression checkMemberSojuCapacity(Double sojuCapacity) {
        val isSojuCapacityEmpty = Objects.isNull(sojuCapacity);
        return isSojuCapacityEmpty ? null : QMember.member.sojuCapacity.eq(sojuCapacity);
    }

    private BooleanExpression checkActivityContainsTeam(String team) {
        val isTeamEmpty = Objects.isNull(team);
        if (isTeamEmpty) return null;
        switch (team) {
            case "임원진" -> {
                return QMemberSoptActivity.memberSoptActivity.part.eq("메이커스 리드")
                        .or(QMemberSoptActivity.memberSoptActivity.part.eq("총무")
                                .or(QMemberSoptActivity.memberSoptActivity.part.contains("장")));
            }
            case "운영팀" -> {
                return QMemberSoptActivity.memberSoptActivity.part.eq("운영 팀장")
                        .or(QMemberSoptActivity.memberSoptActivity.team.eq("운영팀"));
            }
            case "미디어팀" -> {
                return QMemberSoptActivity.memberSoptActivity.part.eq("미디어 팀장")
                        .or(QMemberSoptActivity.memberSoptActivity.team.eq("미디어팀"));
            }
            case "메이커스" -> {
                return QMember.member.id.in(MakersMemberId.getMakersMember());
            }
            default -> {
                return null;
            }
        }
    }

    private BooleanExpression checkNotInWhiteList(QMember member) {
        return member.id.notIn(WHITE_LIST);
    }

    private BooleanExpression checkContainsSearchCond(QMember member, QMemberCareer memberCareer, String cond) {
        if (cond == null) return null;
        return memberCareer.companyName.contains(cond)
            .or(member.name.contains(cond))
            .or(member.university.contains(cond));
    }


    private OrderSpecifier getOrderByCondition(OrderByCondition orderByNum) {
        val orderByNumIsEmpty = Objects.isNull(orderByNum);
        if (orderByNumIsEmpty) return QMember.member.id.desc();
        return switch (orderByNum) {
            case OLDEST_REGISTERED -> QMember.member.id.asc();
            case LATEST_GENERATION -> QMemberSoptActivity.memberSoptActivity.generation.max().desc();
            case OLDEST_GENERATION -> QMemberSoptActivity.memberSoptActivity.generation.min().asc();
            default -> QMember.member.id.desc();
        };
    }

    public List<MemberProfileProjectDao> findMemberProfileProjectsByMemberId (Long memberId) {
        val project = QProject.project;
        val relation = QMemberProjectRelation.memberProjectRelation;
        val member = QMember.member;
        return queryFactory.select(
                        new QMemberProfileProjectDao(
                                project.id, project.writerId, project.name, project.summary, project.generation,
                                project.category, project.logoImage, project.thumbnailImage, project.serviceType
                        )).from(project)
                .innerJoin(relation).on(project.id.eq(relation.projectId))
                .where(relation.isTeamMember.isTrue().and(relation.userId.eq(memberId)))
                .fetch();
    }

    public List<Member> findAllLimitedMemberProfile(String part, Integer limit, Integer cursor, String name, Integer generation) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(checkMemberHasProfile(), checkActivityContainsPart(part), checkUserActivityContainsGeneration(generation), checkMemberContainsName(name))
                .offset(cursor)
                .limit(limit)
                .groupBy(member.id)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllLimitedMemberProfile(
            String part, Integer limit, Integer cursor, String cond,
            Integer generation, Double sojuCapacity, Integer orderBy, String mbti, String team
    ) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        val career = QMemberCareer.memberCareer;

        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .innerJoin(member.careers, career)
                .where(checkMemberHasProfile(),
                        checkContainsSearchCond(member, career, cond),
                        checkMemberSojuCapacity(sojuCapacity),
                        checkMemberMbti(mbti), checkActivityContainsGenerationAndTeamAndPart(generation, team, part),
                        checkNotInWhiteList(member))
                .offset(cursor)
                .limit(limit)
                .groupBy(member.id)
                .orderBy(getOrderByCondition(OrderByCondition.valueOf(orderBy))).fetch();
    }

    public List<Member> findAllMemberProfile(String part, Integer cursor, String name, Integer generation) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(checkMemberHasProfile(), checkActivityContainsPart(part), checkUserActivityContainsGeneration(generation), checkMemberContainsName(name))
                .groupBy(member.id)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllMemberProfile(String part, String cond, Integer generation,
                                             Double sojuCapacity, Integer orderBy, String mbti, String team) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        val career = QMemberCareer.memberCareer;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .innerJoin(member.careers, career)
                .where(checkMemberHasProfile(),
                        checkContainsSearchCond(member, career, cond),
                        checkMemberSojuCapacity(sojuCapacity),
                        checkActivityContainsPart(part), checkMemberMbti(mbti), checkNotInWhiteList(member),
                        checkActivityContainsGenerationAndTeamAndPart(generation, team, part))
                .groupBy(member.id)
                .orderBy(getOrderByCondition(OrderByCondition.valueOf(orderBy)))
                .fetch();
    }

    public List<Member> findAllMemberProfilesBySearchCond(String cond) {
        val member = QMember.member;
        val career = QMemberCareer.memberCareer;
        return queryFactory.selectFrom(member)
            .innerJoin(member.careers, career)
            .where(checkContainsSearchCond(member, career, cond))
            .groupBy(member.id)
            .fetch();
    }


    public List<Long> findAllMemberIdsByGeneration(Integer generation) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.select(member.id)
                .from(member)
                .innerJoin(member.activities, activities)
                .where(checkMemberHasProfile(), checkUserActivityContainsGeneration(generation)
                ).groupBy(member.id)
                .fetch();
    }

    public List<Long> findAllInactivityMemberIdsByGenerationAndPart(Integer generation, Part part) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        if (part != null) {
            return queryFactory.select(member.id)
                    .from(member)
                    .innerJoin(member.activities, activities)
                    .where(activities.generation.ne(generation))
                    .where(activities.part.eq(part.getValue()))
                    .groupBy(member.id)
                    .orderBy(member.id.asc())
                    .fetch();
        } else {
            return queryFactory.select(member.id)
                    .from(member)
                    .innerJoin(member.activities, activities)
                    .where(activities.generation.ne(generation))
                    .groupBy(member.id)
                    .orderBy(member.id.asc())
                    .fetch();
        }
    }


    public int countMembersByGeneration(Integer generation) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.select(member.id)
                .from(member)
                .innerJoin(member.activities, activities).on(activities.memberId.eq(member.id))
                .where(checkMemberHasProfile(), checkUserActivityContainsGeneration(generation))
                .groupBy(member.id)
                .fetch()
                .size();
    }

    public int countAllMemberProfile(String part, String cond, Integer generation, Double sojuCapacity, String mbti, String team) {
        val member = QMember.member;
        val career = QMemberCareer.memberCareer;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.select(member.id)
                .from(member)
                .innerJoin(member.activities, activities)
                .innerJoin(member.careers, career)
                .where(checkMemberHasProfile(),
                        checkContainsSearchCond(member, career, cond),
                        checkMemberSojuCapacity(sojuCapacity),
                        checkActivityContainsPart(part),checkMemberMbti(mbti),
                        checkActivityContainsGenerationAndTeamAndPart(generation, team, part))
                .groupBy(member.id)
                .fetch()
                .size();
    }
}
