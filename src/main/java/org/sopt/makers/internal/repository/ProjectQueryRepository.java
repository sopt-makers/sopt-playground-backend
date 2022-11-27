package org.sopt.makers.internal.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.QProjectLink;
import org.sopt.makers.internal.domain.QMember;
import org.sopt.makers.internal.domain.QMemberProjectRelation;
import org.sopt.makers.internal.domain.QProject;
import org.sopt.makers.internal.dto.project.ProjectDao;
import org.sopt.makers.internal.dto.project.QProjectDao;
import org.sopt.makers.internal.dto.project.ProjectMemberDao;
import org.sopt.makers.internal.dto.project.QProjectMemberDao;
import org.sopt.makers.internal.dto.project.ProjectLinkDao;
import org.sopt.makers.internal.dto.project.QProjectLinkDao;
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
                                member.id, member.name, member.generation, relation.role, relation.description, relation.isTeamMember
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

    public List<ProjectMemberDao> findAll() {
        return getProjectQuery().fetch();
    }

    public List<ProjectLinkDao> findAllLinks() {
        return getProjectLinkQuery().fetch();
    }
}
