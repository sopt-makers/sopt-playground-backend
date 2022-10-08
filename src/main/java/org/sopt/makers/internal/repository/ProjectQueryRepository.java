package org.sopt.makers.internal.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.QLink;
import org.sopt.makers.internal.domain.QMember;
import org.sopt.makers.internal.domain.QMemberProjectRelation;
import org.sopt.makers.internal.domain.QProject;
import org.sopt.makers.internal.dto.ProjectDao;
import org.sopt.makers.internal.dto.QProjectDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProjectQueryRepository {
    private final JPAQueryFactory queryFactory;

    private JPAQuery<ProjectDao> getProjectQuery () {
        val project = QProject.project;
        val member = QMember.member;
        val relation = QMemberProjectRelation.memberProjectRelation;
        val link = QLink.link;

        return queryFactory.select(
                        new QProjectDao(
                                project.id, project.name, project.writerId, project.generation, project.category,
                                project.startAt, project.endAt, project.serviceType, project.isAvailable, project.isFounding,
                                project.summary, project.detail, project.logoImage, project.thumbnailImage, project.images,
                                project.createdAt, project.updatedAt,
                                member.id, member.name, member.generation, relation.role, relation.description, relation.isTeamMember,
                                link.id, link.title, link.url
                        )).from(project)
                .leftJoin(relation).on(relation.projectId.eq(project.id))
                .leftJoin(member).on(relation.userId.eq(member.id))
                .leftJoin(link).on(link.projectId.eq(project.id));
    }

    public List<ProjectDao> findAll() {
        return getProjectQuery().fetch();
    }

    public List<ProjectDao> findById(Long id) {
        val project = QProject.project;
        return getProjectQuery().where(project.id.eq(id)).fetch();
    }
}
