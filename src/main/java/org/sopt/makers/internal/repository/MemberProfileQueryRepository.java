package org.sopt.makers.internal.repository;

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
}
