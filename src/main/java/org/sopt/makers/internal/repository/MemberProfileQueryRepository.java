package org.sopt.makers.internal.repository;

import com.querydsl.core.types.OrderSpecifier;
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
    private OrderSpecifier getOrderByDropDownNumber(Integer orderByDropDown) {
        if (orderByDropDown == null) return QMember.member.id.desc();
        switch (orderByDropDown) {
            case 1 -> { // 최근에 등록했순 (프로필 등록 최근순)
                return QMember.member.id.desc();
            }
            case 2 -> { // 예전에 등록했순 (프로필 등록 오래된순)
                return QMember.member.id.asc();
            }
            case 3 -> { // 최근에 활동했순 (최근 기수순)
                return QMemberSoptActivity.memberSoptActivity.generation.max().desc();
            }
            default -> { // 예전에 활동했순 (오래된 기수순)
                return QMemberSoptActivity.memberSoptActivity.generation.min().asc();
            }
        }
    }
    public List<Member> findAllLimitedMemberProfile(String part, Integer limit, Integer cursor, String name,
                        Integer generation, Double sojuCapactiy, Integer orderByDropDown, String mbti) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(checkMemberHasProfile(), checkActivityContainsPart(part), checkIdGtThanCursor(cursor),
                        checkActivityContainsGeneration(generation), checkMemberContainsName(name),
                        checkMemberMbti(mbti), checkMemberSojuCapactiy(sojuCapactiy))
                .limit(limit)
                .groupBy(member.id)
                .orderBy(getOrderByDropDownNumber(orderByDropDown))
                .fetch();
    }

    public List<Member> findAllMemberProfile(String part, Integer cursor, String name, Integer generation,
                       Double sojuCapactiy, Integer orderByDropDown, String mbti) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(checkMemberHasProfile(), checkActivityContainsPart(part), checkIdGtThanCursor(cursor),
                        checkActivityContainsGeneration(generation), checkMemberContainsName(name),
                        checkMemberMbti(mbti), checkMemberSojuCapactiy(sojuCapactiy))
                .groupBy(member.id)
                .orderBy(getOrderByDropDownNumber(orderByDropDown))
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
