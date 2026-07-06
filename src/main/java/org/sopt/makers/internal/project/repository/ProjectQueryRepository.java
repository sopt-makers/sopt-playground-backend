package org.sopt.makers.internal.project.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.member.domain.QMember;
import org.sopt.makers.internal.project.domain.Project;
import org.sopt.makers.internal.project.domain.QMemberProjectRelation;
import org.sopt.makers.internal.project.domain.QProject;
import org.sopt.makers.internal.project.domain.QProjectLink;
import org.sopt.makers.internal.project.dto.dao.ProjectLinkDao;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProjectQueryRepository {
    private final JPAQueryFactory queryFactory;

    private JPAQuery<ProjectLinkDao> getProjectLinkQuery() {
        val project = QProject.project;
        val projectLink = QProjectLink.projectLink;

        return queryFactory.select(
                Projections.constructor(
                    ProjectLinkDao.class,
                    project.id,
                    project.name,
                    projectLink.id,
                    projectLink.title,
                    projectLink.url
                )
            ).from(project)
            .innerJoin(projectLink).on(projectLink.projectId.eq(project.id));
    }

    public List<ProjectLinkDao> findAllLinks() {
        return getProjectLinkQuery().fetch();
    }

    public List<Project> findProjects(
        Integer limit,
        Long cursor,
        String searchWord,
        String category,
        Boolean isAvailable,
        Boolean isFounding,
        Integer generation
    ) {
        val project = QProject.project;

        JPAQuery<Project> query = queryFactory.selectFrom(project)
            .where(
                ltProjectId(cursor),
                checkProjectContainsSearchWord(searchWord),
                checkProjectIsFounding(isFounding),
                checkProjectCategory(category),
                checkProjectIsAvailable(isAvailable),
                checkProjectGeneration(generation)
            )
            .orderBy(project.id.desc());

        if (limit != null) {
            query.limit(limit);
        }

        return query.fetch();
    }

    public int countAllProjects(
        String searchWord,
        String category,
        Boolean isAvailable,
        Boolean isFounding,
        Integer generation
    ) {
        val project = QProject.project;

        Long count = queryFactory.select(project.id.count())
            .from(project)
            .where(
                checkProjectContainsSearchWord(searchWord),
                checkProjectIsFounding(isFounding),
                checkProjectCategory(category),
                checkProjectIsAvailable(isAvailable),
                checkProjectGeneration(generation)
            )
            .fetchOne();

        return count == null ? 0 : count.intValue();
    }

    public int countProjectsExcludeSopkathon(Long memberId) {
        val member = QMember.member;
        val project = QProject.project;
        val relation = QMemberProjectRelation.memberProjectRelation;

        Long count = queryFactory.select(project.id.countDistinct())
            .from(project)
            .innerJoin(relation).on(relation.projectId.eq(project.id))
            .innerJoin(member).on(relation.userId.eq(member.id))
            .where(
                member.id.eq(memberId),
                project.category.ne("SOPKATHON")
            )
            .fetchOne();

        return count == null ? 0 : count.intValue();
    }

    public List<Project> findRandomProjects(int limit) {
        val project = QProject.project;

        return queryFactory.selectFrom(project)
            .orderBy(Expressions.numberTemplate(Double.class, "random()").asc())
            .limit(limit)
            .fetch();
    }

    private BooleanExpression checkProjectContainsSearchWord(String searchWord) {
        if (searchWord == null || searchWord.trim().isEmpty()) {
            return null;
        }

        val project = QProject.project;
        String likeSearchWord = "%" + escapeLikePattern(searchWord.trim().toLowerCase(Locale.ROOT)) + "%";

        return Expressions.booleanTemplate(
            """
			(
				lower({0}) like {1} escape '\\'
				or lower({2}) like {1} escape '\\'
				or lower({3}) like {1} escape '\\'
			)
			""",
            project.name,
            likeSearchWord,
            project.summary,
            project.detail
        );
    }

    private BooleanExpression checkProjectCategory(String category) {
        if (Objects.isNull(category)) {
            return null;
        }

        return QProject.project.category.eq(category);
    }

    private BooleanExpression checkProjectIsFounding(Boolean isFounding) {
        if (Objects.isNull(isFounding)) {
            return null;
        }

        return QProject.project.isFounding.eq(isFounding);
    }

    private BooleanExpression checkProjectIsAvailable(Boolean isAvailable) {
        if (Objects.isNull(isAvailable)) {
            return null;
        }

        return QProject.project.isAvailable.eq(isAvailable);
    }

    private BooleanExpression checkProjectGeneration(Integer generation) {
        if (Objects.isNull(generation)) {
            return null;
        }

        return QProject.project.generation.eq(generation);
    }

    private BooleanExpression ltProjectId(Long projectId) {
        if (projectId == null || projectId == 0) {
            return null;
        }

        return QProject.project.id.lt(projectId);
    }

    private String escapeLikePattern(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }
}