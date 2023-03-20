package org.sopt.makers.internal.repository;

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

    public List<Member> findAllLimitedMemberProfileByPart(String part, int limit, int cursor) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(member.hasProfile.eq(true)
                        .and(activities.part.contains(part))
                        .and(member.id.gt(cursor))
                ).limit(limit)
                .groupBy(member.id)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllMemberProfileByPart (String part) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(member.hasProfile.eq(true)
                        .and(activities.part.contains(part))
                )
                .groupBy(member.id)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllLimitedMemberProfile(int limit, int cursor) {
        val member = QMember.member;
        return queryFactory.selectFrom(member)
                .where(member.hasProfile.eq(true)
                        .and(member.id.gt(cursor))
                )
                .groupBy(member.id)
                .limit(limit)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllLimitedMemberProfileByPartAndName(String part, int limit, int cursor, String name) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(member.hasProfile.eq(true)
                        .and(activities.part.contains(part))
                        .and(member.id.gt(cursor))
                        .and(member.name.contains(name))
                ).limit(limit)
                .groupBy(member.id)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllMemberProfileByPartAndName (String part, String name) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(member.hasProfile.eq(true)
                        .and(activities.part.contains(part))
                        .and(member.name.contains(name))
                )
                .groupBy(member.id)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllLimitedMemberProfileByName(int limit, int cursor, String name) {
        val member = QMember.member;
        return queryFactory.selectFrom(member)
                .where(member.hasProfile.eq(true)
                        .and(member.id.gt(cursor))
                        .and(member.name.contains(name))
                )
                .groupBy(member.id)
                .limit(limit)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllByName(String name) {
        val member = QMember.member;
        return queryFactory.selectFrom(member)
                .where(member.hasProfile.eq(true)
                        .and(member.name.contains(name))
                )
                .groupBy(member.id)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllLimitedMemberProfileByPartAndGeneration(String part, Integer limit, Integer cursor, Integer generation) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(member.hasProfile.eq(true)
                        .and(activities.part.contains(part))
                        .and(member.id.gt(cursor))
                        .and(activities.generation.in(generation))
                ).limit(limit)
                .groupBy(member.id)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllLimitedMemberProfileByGeneration(Integer generation, Integer limit, Integer cursor) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(member.hasProfile.eq(true)
                        .and(activities.generation.in(generation))
                        .and(member.id.gt(cursor))
                ).limit(limit)
                .groupBy(member.id)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllMemberProfileByPartAndGeneration(String part, Integer generation) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(member.hasProfile.eq(true)
                        .and(activities.part.contains(part))
                        .and(activities.generation.in(generation))
                )
                .groupBy(member.id)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllMemberProfileByGeneration(Integer generation) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        System.out.println(activities.generation.in(generation));
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(member.hasProfile.eq(true)
                        .and(activities.generation.in(generation))
                )
                .groupBy(member.id)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllLimitedMemberProfileByPartAndNameAndGeneration(String part, Integer limit, Integer cursor,
            String name, Integer generation) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(member.hasProfile.eq(true)
                        .and(activities.part.contains(part))
                        .and(member.id.gt(cursor))
                        .and(member.name.contains(name))
                        .and(activities.generation.in(generation))
                ).limit(limit)
                .groupBy(member.id)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllLimitedMemberProfileByGenerationAndName(Integer generation, Integer limit, Integer cursor, String name) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(member.hasProfile.eq(true)
                        .and(activities.generation.in(generation))
                        .and(member.id.gt(cursor))
                        .and(member.name.contains(name))
                ).limit(limit)
                .groupBy(member.id)
                .orderBy(member.id.asc())
                .fetch();
    }

    public List<Member> findAllMemberProfileByGenerationAndName(Integer generation, String name) {
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        return queryFactory.selectFrom(member)
                .innerJoin(member.activities, activities)
                .where(member.hasProfile.eq(true)
                        .and(activities.generation.in(generation))
                        .and(member.name.contains(name))
                )
                .groupBy(member.id)
                .orderBy(member.id.asc())
                .fetch();
    }
}
