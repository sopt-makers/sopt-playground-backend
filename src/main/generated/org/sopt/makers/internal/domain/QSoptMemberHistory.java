package org.sopt.makers.internal.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSoptMemberHistory is a Querydsl query type for SoptMemberHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSoptMemberHistory extends EntityPathBase<SoptMemberHistory> {

    private static final long serialVersionUID = -1371535322L;

    public static final QSoptMemberHistory soptMemberHistory = new QSoptMemberHistory("soptMemberHistory");

    public final StringPath email = createString("email");

    public final NumberPath<Integer> generation = createNumber("generation", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isJoined = createBoolean("isJoined");

    public final StringPath name = createString("name");

    public final StringPath part = createString("part");

    public final StringPath phone = createString("phone");

    public QSoptMemberHistory(String variable) {
        super(SoptMemberHistory.class, forVariable(variable));
    }

    public QSoptMemberHistory(Path<? extends SoptMemberHistory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSoptMemberHistory(PathMetadata metadata) {
        super(SoptMemberHistory.class, metadata);
    }

}

