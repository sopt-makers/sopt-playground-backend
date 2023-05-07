package org.sopt.makers.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.common.MakersMemberId;
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

    private BooleanExpression checkActivityContainsTeam(String team) {
        if(team == null) return null;
        else if(team.equals("임원진"))  {
             return QMemberSoptActivity.memberSoptActivity.part.contains("메이커스 리드")
                    .or(QMemberSoptActivity.memberSoptActivity.part.contains("총무")
                            .or(QMemberSoptActivity.memberSoptActivity.part.contains("장"))); // 회장, 부회장, ~파트장, 운영 팀장, 미디어 팀장)
        } else if(team.equals("운영팀")) {
            return QMemberSoptActivity.memberSoptActivity.part.contains("운영 팀장")
                    .or(QMemberSoptActivity.memberSoptActivity.team.contains("운영팀"));
        } else if(team.equals("미디어팀")) {
            return QMemberSoptActivity.memberSoptActivity.part.contains("미디어 팀장")
                    .or(QMemberSoptActivity.memberSoptActivity.team.contains("미디어팀"));
        } else if(team.equals("메이커스")) {
            return QMember.member.id.in(MakersMemberId.getMakersMember());
        }
        return null;
    }

    private OrderSpecifier getOrderByCondition(OrderByCondition orderByNum) {
        if(orderByNum == null) return QMember.member.id.desc();
        else if(orderByNum == OLDEST_REGISTERED) return QMember.member.id.asc();
        else if(orderByNum == LATEST_GENERATION) return QMemberSoptActivity.memberSoptActivity.generation.max().desc();
        else if(orderByNum == OLDEST_GENERATION) return QMemberSoptActivity.memberSoptActivity.generation.min().asc();
        else return QMember.member.id.desc();
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
                .where(checkMemberHasProfile(), checkIdGtThanCursor(cursor),
                        checkMemberContainsName(name), checkMemberSojuCapactiy(sojuCapactiy),
                        checkActivityContainsGeneration(generation), checkActivityContainsPart(part),
                        checkActivityContainsTeam(team), checkMemberMbti(mbti))
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
                .where(checkMemberHasProfile(), checkIdGtThanCursor(cursor),
                        checkMemberContainsName(name), checkMemberSojuCapactiy(sojuCapactiy),
                        checkActivityContainsGeneration(generation), checkActivityContainsPart(part),
                        checkActivityContainsTeam(team), checkMemberMbti(mbti))
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
