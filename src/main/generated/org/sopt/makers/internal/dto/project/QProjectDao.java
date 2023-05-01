package org.sopt.makers.internal.dto.project;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * org.sopt.makers.internal.dto.project.QProjectDao is a Querydsl Projection type for ProjectDao
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QProjectDao extends ConstructorExpression<ProjectDao> {

    private static final long serialVersionUID = 715369875L;

    public QProjectDao(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> name, com.querydsl.core.types.Expression<Long> writerId, com.querydsl.core.types.Expression<Integer> generation, com.querydsl.core.types.Expression<String> category, com.querydsl.core.types.Expression<java.time.LocalDate> startAt, com.querydsl.core.types.Expression<java.time.LocalDate> endAt, com.querydsl.core.types.Expression<? extends String[]> serviceType, com.querydsl.core.types.Expression<Boolean> isAvailable, com.querydsl.core.types.Expression<Boolean> isFounding, com.querydsl.core.types.Expression<String> summary, com.querydsl.core.types.Expression<String> detail, com.querydsl.core.types.Expression<String> logoImage, com.querydsl.core.types.Expression<String> thumbnailImage, com.querydsl.core.types.Expression<? extends String[]> images, com.querydsl.core.types.Expression<java.time.LocalDateTime> createdAt, com.querydsl.core.types.Expression<java.time.LocalDateTime> updatedAt, com.querydsl.core.types.Expression<Long> memberId, com.querydsl.core.types.Expression<String> memberName, com.querydsl.core.types.Expression<Integer> memberGeneration, com.querydsl.core.types.Expression<String> memberRole, com.querydsl.core.types.Expression<String> memberDesc, com.querydsl.core.types.Expression<Boolean> isTeamMember, com.querydsl.core.types.Expression<Long> linkId, com.querydsl.core.types.Expression<String> linkTitle, com.querydsl.core.types.Expression<String> linkUrl) {
        super(ProjectDao.class, new Class<?>[]{long.class, String.class, long.class, int.class, String.class, java.time.LocalDate.class, java.time.LocalDate.class, String[].class, boolean.class, boolean.class, String.class, String.class, String.class, String.class, String[].class, java.time.LocalDateTime.class, java.time.LocalDateTime.class, long.class, String.class, int.class, String.class, String.class, boolean.class, long.class, String.class, String.class}, id, name, writerId, generation, category, startAt, endAt, serviceType, isAvailable, isFounding, summary, detail, logoImage, thumbnailImage, images, createdAt, updatedAt, memberId, memberName, memberGeneration, memberRole, memberDesc, isTeamMember, linkId, linkTitle, linkUrl);
    }

}

