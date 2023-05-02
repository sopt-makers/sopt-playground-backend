package org.sopt.makers.internal.repository;

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

    private BooleanExpression checkActivityContainsPartAndGeneration(String part, Integer generation) {
        if (part == null) return null;
        return QMemberSoptActivity.memberSoptActivity.part.contains(part)
                .and(QMemberSoptActivity.memberSoptActivity.generation.eq(generation));
    }

    private BooleanExpression checkIdGtThanCursor(Integer cursor) {
        if(cursor == null) return null;
        return QMember.member.id.gt(cursor);
    }

    private BooleanExpression checkActivityContainsGeneration(Integer generation) {
        if(generation == null) return null;
        return QMemberSoptActivity.memberSoptActivity.generation.eq(generation);
    }

    private BooleanExpression checkMemberHasProfile() {
        return QMember.member.hasProfile.eq(true);
    }

    private BooleanExpression checkMemberMbti(String mbti) {
        if(mbti == null) return null;
        return QMember.member.mbti.eq(mbti);
    }

    private BooleanExpression checkMemberSojuCapactiy(Double sojuCapactiy) {
        if(sojuCapactiy == null) return null;
        return QMember.member.sojuCapacity.eq(sojuCapactiy);
    }

    private OrderSpecifier getOrderByCondition(OrderByCondition sortCondition) {
        switch (sortCondition) {
            case OLDEST_REGISTERED -> {
                return QMember.member.id.asc();
            }
            case LATEST_GENERATION -> {
                return QMemberSoptActivity.memberSoptActivity.generation.max().desc();
            }
            case OLDEST_GENERATION -> {
                return QMemberSoptActivity.memberSoptActivity.generation.min().asc();
            }
            default -> {
                return QMember.member.id.desc();
            }
        }
    }

    private Predicate checkMemberGenerationAndTeamAndPart(Integer generation, String team, String part) {
        if(generation == null) {
            if(part == null && team == null) return null;
            else if(part == null && team != null) return checkMemberBelongToTeam(team);
            else if(team == null && part != null) return checkActivityContainsPart(part);
            else return checkMemberBelongToTeamAndPart(team, part);
        }
        else {
            if(team == null && part == null) return checkActivityContainsGeneration(generation);
            else if(team == null && part != null) return checkActivityContainsPartAndGeneration(part, generation);
            else if(part == null) return checkMemberBelongToTeamAndGeneration(team, generation);
            else return checkMemberBelongToTeamAndPartAndGeneration(team, part, generation);
        }
    }

    private BooleanExpression checkMemberBelongToTeamAndPart(String team, String part) {
        if (team == null || part == null) return null;
        switch (team) {
            case "임원진" -> {
                return QMemberSoptActivity.memberSoptActivity.part.contains(part)
                        .and(QMemberSoptActivity.memberSoptActivity.part.contains("메이커스 리드"))
                                .or(QMemberSoptActivity.memberSoptActivity.part.contains("총무")
                                .or(QMemberSoptActivity.memberSoptActivity.part.contains("장"))); // 회장, 부회장, ~파트장, 운영 팀장, 미디어 팀장
            }
            case "운영팀" -> {
                return QMemberSoptActivity.memberSoptActivity.part.contains(part)
                        .and(QMemberSoptActivity.memberSoptActivity.team.contains("운영팀"))
                                .or(QMemberSoptActivity.memberSoptActivity.part.contains("운영 팀장"));
            }
            case "미디어팀" -> {
                return QMemberSoptActivity.memberSoptActivity.part.contains(part)
                        .and(QMemberSoptActivity.memberSoptActivity.team.contains("미디어팀"))
                                .or(QMemberSoptActivity.memberSoptActivity.part.contains("미디어 팀장"));
            }
            case "메이커스" -> {
                return QMemberSoptActivity.memberSoptActivity.part.contains(part)
                        .and(QMember.member.id.in(
                                1L, 44L, 23L, 31L, 8L, 30L, 40L, 46L, 26L, 60L, 39L, 6L, 9L, 7L, 2L, 3L, 29L,
                        5L, 38L, 37L, 13L, 28L, 36L, 58L, 173L, 32L, 43L, 188L, 59L, 34L, 21L, 33L, 22L, 35L, 45L,
                        186L, 227L, 264L, 4L, 51L, 187L, 128L, 64L, 99L, 10L, 66L, 260L, 72L, 265L, 78L, 251L,
                        115L, 258L, 112L, 205L, 238L, 259L, 281L, 285L, 286L, 283L, 282L));
            }
            default -> {
                return null;
            }
        }
    }

    private BooleanExpression checkMemberBelongToTeamAndPartAndGeneration(String team, String part, Integer generation) {
        if (team == null || part == null) return null;
        switch (team) {
            case "임원진" -> {
                return QMemberSoptActivity.memberSoptActivity.generation.eq(generation)
                        .and(QMemberSoptActivity.memberSoptActivity.part.contains(part)
                        .and(QMemberSoptActivity.memberSoptActivity.part.contains("메이커스 리드"))
                                .or(QMemberSoptActivity.memberSoptActivity.part.contains("총무")
                                .or(QMemberSoptActivity.memberSoptActivity.part.contains("장")))); // 회장, 부회장, ~파트장, 운영 팀장, 미디어 팀장
            }
            case "운영팀" -> {
                return QMemberSoptActivity.memberSoptActivity.generation.eq(generation)
                        .and(QMemberSoptActivity.memberSoptActivity.part.contains(part)
                        .and(QMemberSoptActivity.memberSoptActivity.team.contains("운영팀"))
                                .or(QMemberSoptActivity.memberSoptActivity.part.contains("운영 팀장")));
            }
            case "미디어팀" -> {
                return QMemberSoptActivity.memberSoptActivity.generation.eq(generation)
                        .and(QMemberSoptActivity.memberSoptActivity.part.contains(part)
                        .and(QMemberSoptActivity.memberSoptActivity.team.contains("미디어팀"))
                                .or(QMemberSoptActivity.memberSoptActivity.part.contains("미디어 팀장")));
            }
            case "메이커스" -> {
                return QMemberSoptActivity.memberSoptActivity.generation.eq(generation)
                        .and(QMemberSoptActivity.memberSoptActivity.part.contains(part)
                        .and(QMember.member.id.in(
                                1L, 44L, 23L, 31L, 8L, 30L, 40L, 46L, 26L, 60L, 39L, 6L, 9L, 7L, 2L, 3L, 29L,
                        5L, 38L, 37L, 13L, 28L, 36L, 58L, 173L, 32L, 43L, 188L, 59L, 34L, 21L, 33L, 22L, 35L, 45L,
                        186L, 227L, 264L, 4L, 51L, 187L, 128L, 64L, 99L, 10L, 66L, 260L, 72L, 265L, 78L, 251L,
                        115L, 258L, 112L, 205L, 238L, 259L, 281L, 285L, 286L, 283L, 282L)));
            }
            default -> {
                return null;
            }
        }
    }

    private BooleanExpression checkMemberBelongToTeam(String team) {
        if (team == null) return null;
        switch (team) {
            case "임원진" -> {
                return QMemberSoptActivity.memberSoptActivity.part.contains("메이커스 리드")
                        .or(QMemberSoptActivity.memberSoptActivity.part.contains("총무")
                        .or(QMemberSoptActivity.memberSoptActivity.part.contains("장"))); // 회장, 부회장, ~파트장, 운영 팀장, 미디어 팀장
            }
            case "운영팀" -> {
                return QMemberSoptActivity.memberSoptActivity.team.contains("운영팀")
                        .or(QMemberSoptActivity.memberSoptActivity.part.contains("운영 팀장"));
            }
            case "미디어팀" -> {
                return QMemberSoptActivity.memberSoptActivity.team.contains("미디어팀")
                        .or(QMemberSoptActivity.memberSoptActivity.part.contains("미디어 팀장"));
            }
            case "메이커스" -> {
                return QMember.member.id.in(
                        1L, 44L, 23L, 31L, 8L, 30L, 40L, 46L, 26L, 60L, 39L, 6L, 9L, 7L, 2L, 3L, 29L,
                        5L, 38L, 37L, 13L, 28L, 36L, 58L, 173L, 32L, 43L, 188L, 59L, 34L, 21L, 33L, 22L, 35L, 45L,
                        186L, 227L, 264L, 4L, 51L, 187L, 128L, 64L, 99L, 10L, 66L, 260L, 72L, 265L, 78L, 251L,
                        115L, 258L, 112L, 205L, 238L, 259L, 281L, 285L, 286L, 283L, 282L);
            }
            default -> {
                return null;
            }
        }
    }

    private BooleanExpression checkMemberBelongToTeamAndGeneration(String team, Integer generation) {
        if (team == null) return null;
        switch (team) {
            case "임원진" -> {
                return QMemberSoptActivity.memberSoptActivity.generation.eq(generation)
                        .and(QMemberSoptActivity.memberSoptActivity.part.contains("메이커스 리드")
                        .or(QMemberSoptActivity.memberSoptActivity.part.contains("총무")
                        .or(QMemberSoptActivity.memberSoptActivity.part.contains("장")))); // 회장, 부회장, ~파트장, 운영 팀장, 미디어 팀장
            }
            case "운영팀" -> {
                return QMemberSoptActivity.memberSoptActivity.generation.eq(generation)
                        .and(QMemberSoptActivity.memberSoptActivity.team.contains("운영팀")
                        .or(QMemberSoptActivity.memberSoptActivity.part.contains("운영 팀장")));
            }
            case "미디어팀" -> {
                return QMemberSoptActivity.memberSoptActivity.generation.eq(generation)
                        .and(QMemberSoptActivity.memberSoptActivity.team.contains("미디어팀")
                        .or(QMemberSoptActivity.memberSoptActivity.part.contains("미디어 팀장")));
            }
            case "메이커스" -> {
                return QMemberSoptActivity.memberSoptActivity.generation.eq(generation)
                        .and(QMember.member.id.in(
                        1L, 44L, 23L, 31L, 8L, 30L, 40L, 46L, 26L, 60L, 39L, 6L, 9L, 7L, 2L, 3L, 29L,
                        5L, 38L, 37L, 13L, 28L, 36L, 58L, 173L, 32L, 43L, 188L, 59L, 34L, 21L, 33L, 22L, 35L, 45L,
                        186L, 227L, 264L, 4L, 51L, 187L, 128L, 64L, 99L, 10L, 66L, 260L, 72L, 265L, 78L, 251L,
                        115L, 258L, 112L, 205L, 238L, 259L, 281L, 285L, 286L, 283L, 282L));
            }
            default -> {
                return null;
            }
        }
    }

    public List<Member> findAllLimitedMemberProfile(String part, Integer limit, Integer cursor, String name, Integer generation) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(checkMemberHasProfile(), checkActivityContainsPart(part), checkIdGtThanCursor(cursor),
                        checkActivityContainsGeneration(generation), checkMemberContainsName(name))
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
                .where(checkMemberHasProfile(), checkIdGtThanCursor(cursor),
                        checkMemberGenerationAndTeamAndPart(generation, team, part),
                        checkMemberContainsName(name), checkMemberMbti(mbti), checkMemberSojuCapactiy(sojuCapactiy))
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
                .where(checkMemberHasProfile(), checkActivityContainsPart(part), checkIdGtThanCursor(cursor),
                        checkActivityContainsGeneration(generation), checkMemberContainsName(name))
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
                .where(checkMemberHasProfile(), checkIdGtThanCursor(cursor),
                        checkMemberGenerationAndTeamAndPart(generation, team, part),
                        checkMemberContainsName(name), checkMemberMbti(mbti), checkMemberSojuCapactiy(sojuCapactiy))
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
