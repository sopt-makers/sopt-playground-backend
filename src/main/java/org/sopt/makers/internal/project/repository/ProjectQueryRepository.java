package org.sopt.makers.internal.project.repository;

import java.util.List;
import java.util.Objects;

import org.sopt.makers.internal.member.domain.QMember;
import org.sopt.makers.internal.project.domain.Project;
import org.sopt.makers.internal.project.domain.QMemberProjectRelation;
import org.sopt.makers.internal.project.domain.QProject;
import org.sopt.makers.internal.project.domain.QProjectLink;
import org.sopt.makers.internal.project.dto.response.ProjectLinkDao;
import org.sopt.makers.internal.project.dto.response.ProjectMemberDao;
import org.sopt.makers.internal.project.dto.response.QProjectLinkDao;
import org.sopt.makers.internal.project.dto.response.QProjectMemberDao;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.val;

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

    private BooleanExpression checkProjectContainsName(String name) {
        val checkNameIsEmpty = Objects.isNull(name);
        return checkNameIsEmpty ? null : QProject.project.name.lower().contains(name.toLowerCase());
    }

    private BooleanExpression checkProjectCategory(String category) {
        val checkCategoryIsEmpty = Objects.isNull(category);
        return checkCategoryIsEmpty ? null : QProject.project.category.eq(category);
    }

    private BooleanExpression checkProjectIsFounding(Boolean isFounding) {
        val checkIsFoundingIsEmpty = Objects.isNull(isFounding);
        return checkIsFoundingIsEmpty || isFounding.equals(Boolean.FALSE) ? null : QProject.project.isFounding.eq(isFounding);
    }

    private BooleanExpression checkProjectIsAvailable(Boolean isAvailable) {
        val checkIsAvailableIsEmpty = Objects.isNull(isAvailable);
        return checkIsAvailableIsEmpty || isAvailable.equals(Boolean.FALSE) ? null : QProject.project.isAvailable.eq(isAvailable);
    }

    private BooleanExpression ltProjectId(Long projectId) {
        val project = QProject.project;
        if(projectId == null || projectId == 0) return null;
        return project.id.lt(projectId);
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

        return queryFactory.selectFrom(project)
                .where(checkProjectContainsName(name), checkProjectIsFounding(isFounding),
                        checkProjectCategory(category), checkProjectIsAvailable(isAvailable))
                .orderBy(project.id.desc())
                .groupBy(project.id)
                .fetch();
    }

    public List<Project> findAllLimitedProjects(
            Integer limit, Long cursor, String category, Boolean isAvailable, Boolean isFounding
    ) {
        val project = QProject.project;

        return queryFactory.selectFrom(project)
                .where(ltProjectId(cursor), checkProjectIsFounding(isFounding),
                        checkProjectCategory(category), checkProjectIsAvailable(isAvailable))
                .limit(limit)
                .orderBy(project.id.desc())
                .groupBy(project.id)
                .fetch();
    }

    public List<Project> findAllLimitedProjectsContainsName(
            Integer limit, Long cursor, String name, String category, Boolean isAvailable, Boolean isFounding
    ) {
        val project = QProject.project;

        return queryFactory.selectFrom(project)
                .where(ltProjectId(cursor), checkProjectContainsName(name), checkProjectIsFounding(isFounding),
                        checkProjectCategory(category), checkProjectIsAvailable(isAvailable))
                .limit(limit)
                .orderBy(project.id.desc())
                .groupBy(project.id)
                .fetch();
    }

    public int countAllProjects(String name, String category, Boolean isAvailable, Boolean isFounding) {
        val project = QProject.project;
        return queryFactory.select(project.id)
                .from(project)
                .where(checkProjectContainsName(name), checkProjectIsFounding(isFounding),
                        checkProjectCategory(category), checkProjectIsAvailable(isAvailable))
                .groupBy(project.id)
                .fetch()
                .size();
    }

    public int countProjectsExcludeSopkathon(Long memberId) {
        QMember member = QMember.member;
        QProject project = QProject.project;
        QMemberProjectRelation relation = QMemberProjectRelation.memberProjectRelation;

        return queryFactory.select(project.id)
                .from(project)
                .innerJoin(relation).on(relation.projectId.eq(project.id))
                .innerJoin(member).on(relation.userId.eq(member.id))
                .where(
                    member.id.eq(memberId)
                    .and(project.category.ne("SOPKATHON")))
                .fetch()
                .size();
    }
}
