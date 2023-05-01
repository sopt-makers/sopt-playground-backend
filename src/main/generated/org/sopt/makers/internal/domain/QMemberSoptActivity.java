package org.sopt.makers.internal.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMemberSoptActivity is a Querydsl query type for MemberSoptActivity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberSoptActivity extends EntityPathBase<MemberSoptActivity> {

    private static final long serialVersionUID = 114586813L;

    public static final QMemberSoptActivity memberSoptActivity = new QMemberSoptActivity("memberSoptActivity");

    public final NumberPath<Integer> generation = createNumber("generation", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final StringPath part = createString("part");

    public final StringPath team = createString("team");

    public QMemberSoptActivity(String variable) {
        super(MemberSoptActivity.class, forVariable(variable));
    }

    public QMemberSoptActivity(Path<? extends MemberSoptActivity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMemberSoptActivity(PathMetadata metadata) {
        super(MemberSoptActivity.class, metadata);
    }

}

