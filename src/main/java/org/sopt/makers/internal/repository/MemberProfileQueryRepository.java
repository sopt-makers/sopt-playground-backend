package org.sopt.makers.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.*;
import org.sopt.makers.internal.dto.member.MemberProfileProjectDao;
import org.sopt.makers.internal.dto.member.QMemberProfileProjectDao;
import org.springframework.stereotype.Repository;
import static org.sopt.makers.internal.domain.OrderByCondition.*;

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
                        project.category, project.logoImage, project.thumbnailImage, project.serviceType
                )).from(project)
                .innerJoin(relation).on(project.id.eq(relation.projectId))
                .where(relation.isTeamMember.isTrue().and(relation.userId.eq(memberId)))
                .fetch();
    }

    private BooleanExpression checkMemberContainsName(String name) {
        if(name == null) return null;
        return QMember.member.name.contains(name);
    }

    private BooleanExpression checkActivityContainsPart(String part) {
        if(part == null) return null;
        return QMemberSoptActivity.memberSoptActivity.part.contains(part);
    }

    private BooleanExpression checkIdGtThanCursor(Integer cursor) {
        if(cursor == null) return null;
        return QMember.member.id.gt(cursor);
    }

    private Predicate checkActivityContainsGeneration(Integer generation) {
        if(generation == null) return null;
        return QMemberSoptActivity.memberSoptActivity.generation.eq(generation);
    }

    private BooleanExpression checkMemberHasProfile() {
        return QMember.member.hasProfile.eq(true);
    }

    private OrderSpecifier getOrderByCondition(OrderByCondition orderByNum) {
        if(orderByNum == null) return QMember.member.id.desc();
        else if(orderByNum == OLDEST_REGISTERED) return QMember.member.id.asc();
        else if(orderByNum == LATEST_GENERATION) return QMemberSoptActivity.memberSoptActivity.generation.max().desc();
        else if(orderByNum == OLDEST_GENERATION) return QMemberSoptActivity.memberSoptActivity.generation.min().asc();
        else return QMember.member.id.desc();
    }

    public Predicate checkWhereConditionsInPart(String part, String team) {
        if(part != null && team != null) {
            if(team.equals("임원진"))  {
                return QMemberSoptActivity.memberSoptActivity.part.contains(part)
                        .and(QMemberSoptActivity.memberSoptActivity.part.contains("메이커스 리드"))
                        .or(QMemberSoptActivity.memberSoptActivity.part.contains("총무")
                                .or(QMemberSoptActivity.memberSoptActivity.part.contains("장"))); // 회장, 부회장, ~파트장, 운영 팀장, 미디어 팀장)
            } else if(team.equals("운영팀")) {
                return QMemberSoptActivity.memberSoptActivity.part.contains(part)
                        .and(QMemberSoptActivity.memberSoptActivity.team.contains("운영팀"))
                        .or(QMemberSoptActivity.memberSoptActivity.part.contains("운영 팀장"));
            } else if(team.equals("미디어팀")) {
                return QMemberSoptActivity.memberSoptActivity.part.contains(part)
                        .and(QMemberSoptActivity.memberSoptActivity.team.contains("미디어팀"))
                        .or(QMemberSoptActivity.memberSoptActivity.part.contains("미디어 팀장"));
            } else if(team.equals("메이커스")) {
                return QMemberSoptActivity.memberSoptActivity.part.contains(part)
                .and(QMember.member.id.in(
                        1L, 44L, 23L, 31L, 8L, 30L, 40L, 46L, 26L, 60L, 39L, 6L, 9L, 7L, 2L, 3L, 29L,
                        5L, 38L, 37L, 13L, 28L, 36L, 58L, 173L, 32L, 43L, 188L, 59L, 34L, 21L, 33L, 22L, 35L, 45L,
                        186L, 227L, 264L, 4L, 51L, 187L, 128L, 64L, 99L, 10L, 66L, 260L, 72L, 265L, 78L, 251L,
                        115L, 258L, 112L, 205L, 238L, 259L, 281L, 285L, 286L, 283L, 282L));
            }
        }
        return null;
    }

    public BooleanBuilder checkWhereConditions(String part, String name, Integer cursor,
                                               Integer generation, Double sojuCapactiy, String mbti, String team) {
        val builder = new BooleanBuilder();
        if(cursor != null) {
            builder.and(QMember.member.id.gt(cursor));
        }
        if(name != null) {
            builder.and(QMember.member.name.contains(name));
        }
        if(generation != null) {
            builder.and(QMemberSoptActivity.memberSoptActivity.generation.eq(generation));
        }
        if(sojuCapactiy != null) {
            builder.and(QMember.member.sojuCapacity.eq(sojuCapactiy));
        }
        if(mbti != null) {
            builder.and(QMember.member.mbti.eq(mbti));
        }
        if(part != null && team != null) {
            builder.and(checkWhereConditionsInPart(part, team));
        }
        if(part != null && team == null) {
            builder.and(QMemberSoptActivity.memberSoptActivity.part.contains(part));
        }
        if(team != null && part == null) {
            if(team.equals("임원진"))  {
                builder.and(QMemberSoptActivity.memberSoptActivity.part.contains("메이커스 리드"))
                        .or(QMemberSoptActivity.memberSoptActivity.part.contains("총무")
                                .or(QMemberSoptActivity.memberSoptActivity.part.contains("장"))); // 회장, 부회장, ~파트장, 운영 팀장, 미디어 팀장)
            } else if(team.equals("운영팀")) {
                builder.and(QMemberSoptActivity.memberSoptActivity.team.contains("운영팀"))
                        .or(QMemberSoptActivity.memberSoptActivity.part.contains("운영 팀장"));
            } else if(team.equals("미디어팀")) {
                builder.and(QMemberSoptActivity.memberSoptActivity.team.contains("미디어팀"))
                        .or(QMemberSoptActivity.memberSoptActivity.part.contains("미디어 팀장"));
            } else if(team.equals("메이커스")) {
                builder.and(QMember.member.id.in(
                        1L, 44L, 23L, 31L, 8L, 30L, 40L, 46L, 26L, 60L, 39L, 6L, 9L, 7L, 2L, 3L, 29L,
                        5L, 38L, 37L, 13L, 28L, 36L, 58L, 173L, 32L, 43L, 188L, 59L, 34L, 21L, 33L, 22L, 35L, 45L,
                        186L, 227L, 264L, 4L, 51L, 187L, 128L, 64L, 99L, 10L, 66L, 260L, 72L, 265L, 78L, 251L,
                        115L, 258L, 112L, 205L, 238L, 259L, 281L, 285L, 286L, 283L, 282L));
            }
        }
        return builder;
    }

    public List<Member> findAllLimitedMemberProfile(String part, Integer limit, Integer cursor, String name, Integer generation) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(checkMemberHasProfile(), checkActivityContainsPart(part), checkIdGtThanCursor(cursor), checkActivityContainsGeneration(generation), checkMemberContainsName(name))
                .limit(limit)
                .groupBy(member.id)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllLimitedMemberProfile(String part, Integer limit, Integer cursor, String name,
                                                    Integer generation, Double sojuCapactiy, Integer orderBy, String mbti, String team) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(checkMemberHasProfile(),
                        checkWhereConditions(part, name, cursor, generation, sojuCapactiy, mbti, team)
                )
                .limit(limit)
                .groupBy(member.id)
                .orderBy(getOrderByCondition(OrderByCondition.valueOf(orderBy)))
                .fetch();
    }

    public List<Member> findAllMemberProfile(String part, Integer cursor, String name, Integer generation) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(checkMemberHasProfile(), checkActivityContainsPart(part), checkIdGtThanCursor(cursor), checkActivityContainsGeneration(generation), checkMemberContainsName(name))
                .groupBy(member.id)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllMemberProfile(String part, Integer cursor, String name, Integer generation,
                                             Double sojuCapactiy, Integer orderBy, String mbti, String team) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(checkMemberHasProfile(),
                        checkWhereConditions(part, name, cursor, generation, sojuCapactiy, mbti, team))
                .groupBy(member.id)
                .orderBy(getOrderByCondition(OrderByCondition.valueOf(orderBy)))
                .fetch();
    }

    public int countMembersByGeneration(Integer generation) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.select(member.id)
                .from(member)
                .innerJoin(member.activities, activities).on(activities.memberId.eq(member.id))
                .where(checkMemberHasProfile(), checkActivityContainsGeneration(generation))
                .groupBy(member.id)
                .fetch()
                .size();
    }
}
