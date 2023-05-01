package org.sopt.makers.internal.dto.project;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * org.sopt.makers.internal.dto.project.QProjectLinkDao is a Querydsl Projection type for ProjectLinkDao
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QProjectLinkDao extends ConstructorExpression<ProjectLinkDao> {

    private static final long serialVersionUID = 1679352505L;

    public QProjectLinkDao(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> name, com.querydsl.core.types.Expression<Long> linkId, com.querydsl.core.types.Expression<String> linkTitle, com.querydsl.core.types.Expression<String> linkUrl) {
        super(ProjectLinkDao.class, new Class<?>[]{long.class, String.class, long.class, String.class, String.class}, id, name, linkId, linkTitle, linkUrl);
    }

}

