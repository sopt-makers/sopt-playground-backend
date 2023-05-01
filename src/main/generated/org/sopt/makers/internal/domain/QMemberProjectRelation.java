package org.sopt.makers.internal.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMemberProjectRelation is a Querydsl query type for MemberProjectRelation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberProjectRelation extends EntityPathBase<MemberProjectRelation> {

    private static final long serialVersionUID = 1174856007L;

    public static final QMemberProjectRelation memberProjectRelation = new QMemberProjectRelation("memberProjectRelation");

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isTeamMember = createBoolean("isTeamMember");

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final StringPath role = createString("role");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QMemberProjectRelation(String variable) {
        super(MemberProjectRelation.class, forVariable(variable));
    }

    public QMemberProjectRelation(Path<? extends MemberProjectRelation> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMemberProjectRelation(PathMetadata metadata) {
        super(MemberProjectRelation.class, metadata);
    }

}

