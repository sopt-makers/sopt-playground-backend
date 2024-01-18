package org.sopt.makers.internal.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.*;
import org.sopt.makers.internal.dto.project.ProjectLinkDao;
import org.sopt.makers.internal.dto.project.ProjectMemberDao;
import org.sopt.makers.internal.dto.project.QProjectLinkDao;
import org.sopt.makers.internal.dto.project.QProjectMemberDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProjectQueryRepository {
    private final JPAQueryFactory queryFactory;

    private JPAQuery<ProjectMemberDao> getProjectQuery () {
        val project = QProject.project;
        val member = QMember.member;
        val relation = QMemberProjectRelation.memberProjectRelation;

        return queryFactory.select(
                        new QProjectMemberDao(
                                project.id, project.name, project.writerId, project.generation, project.category,
                                project.startAt, project.endAt, project.serviceType, project.isAvailable, project.isFounding,
                                project.summary, project.detail, project.logoImage, project.thumbnailImage, project.images,
                                project.createdAt, project.updatedAt,
                                member.id, member.name, member.generation, member.profileImage, member.hasProfile,
                                relation.role, relation.description, relation.isTeamMember
                        )).from(project)
                .innerJoin(relation).on(relation.projectId.eq(project.id))
                .innerJoin(member).on(relation.userId.eq(member.id));
    }

    private JPAQuery<ProjectLinkDao> getProjectLinkQuery () {
        val project = QProject.project;
        val projectLink = QProjectLink.projectLink;

        return queryFactory.select(
                        new QProjectLinkDao(
                                project.id, project.name,
                                projectLink.id, projectLink.title, projectLink.url
                        )).from(project)
                .innerJoin(projectLink).on(projectLink.projectId.eq(project.id));
    }

    public List<ProjectMemberDao> findById(Long id) {
        val project = QProject.project;
        return getProjectQuery().where(project.id.eq(id)).fetch();
    }

    public List<ProjectLinkDao> findLinksById(Long id) {
        val project = QProject.project;
        return getProjectLinkQuery().where(project.id.eq(id)).fetch();
    }
    
    public List<ProjectLinkDao> findAllLinks() {
        return getProjectLinkQuery().fetch();
    }

    public List<Project> findAllNameProjects(
            String name, String category, Boolean isAvailable, Boolean isFounding
    ) {
        val project = QProject.project;

        BooleanExpression filter = project.isNotNull();
        if (category != null) {
            filter = filter.and(project.category.eq(category));
        }

        if (isAvailable != null) {
            filter = filter.and(project.isAvailable.eq(isAvailable));
        }

        if (isFounding != null) {
            filter = filter.and(project.isFounding.eq(isFounding));
        }

        return queryFactory.selectFrom(project)
                .where(project.name.contains(name), filter)
                .orderBy(project.id.desc())
                .groupBy(project.id)
                .fetch();
    }

    public List<Project> findAllLimitedProjects(
            Integer limit, Long cursor, String category, Boolean isAvailable, Boolean isFounding
    ) {
        val project = QProject.project;

        BooleanExpression filter = project.isNotNull();
        if (category != null) {
            filter = filter.and(project.category.eq(category));
        }

        if (isAvailable != null) {
            filter = filter.and(project.isAvailable.eq(isAvailable));
        }

        if (isFounding != null) {
            filter = filter.and(project.isFounding.eq(isFounding));
        }

        return queryFactory.selectFrom(project)
                .where(ltProjectId(cursor), filter)
                .limit(limit)
                .orderBy(project.id.desc())
                .groupBy(project.id)
                .fetch();
    }

    public List<Project> findAllLimitedProjectsContainsName(
            Integer limit, Long cursor, String name, String category, Boolean isAvailable, Boolean isFounding
    ) {
        val project = QProject.project;

        BooleanExpression filter = project.isNotNull();
        if (category != null) {
            filter = filter.and(project.category.eq(category));
        }

        if (isAvailable != null) {
            filter = filter.and(project.isAvailable.eq(isAvailable));
        }

        if (isFounding != null) {
            filter = filter.and(project.isFounding.eq(isFounding));
        }

        return queryFactory.selectFrom(project)
                .where(ltProjectId(cursor), project.name.contains(name), filter)
                .limit(limit)
                .orderBy(project.id.desc())
                .groupBy(project.id)
                .fetch();
    }

    private BooleanExpression ltProjectId(Long projectId) {
        val project = QProject.project;
        if(projectId == null || projectId == 0) return null;
        return project.id.lt(projectId);
    }
}
