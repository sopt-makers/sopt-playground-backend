package org.sopt.makers.internal.dto.member;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * org.sopt.makers.internal.dto.member.QMemberProfileProjectDao is a Querydsl Projection type for MemberProfileProjectDao
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QMemberProfileProjectDao extends ConstructorExpression<MemberProfileProjectDao> {

    private static final long serialVersionUID = 12041113L;

    public QMemberProfileProjectDao(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<Long> writerId, com.querydsl.core.types.Expression<String> name, com.querydsl.core.types.Expression<String> summary, com.querydsl.core.types.Expression<Integer> generation, com.querydsl.core.types.Expression<String> category, com.querydsl.core.types.Expression<String> logoImage, com.querydsl.core.types.Expression<String> thumbnailImage, com.querydsl.core.types.Expression<? extends String[]> serviceType) {
        super(MemberProfileProjectDao.class, new Class<?>[]{long.class, long.class, String.class, String.class, int.class, String.class, String.class, String.class, String[].class}, id, writerId, name, summary, generation, category, logoImage, thumbnailImage, serviceType);
    }

}

