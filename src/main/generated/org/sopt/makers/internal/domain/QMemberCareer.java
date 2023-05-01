package org.sopt.makers.internal.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMemberCareer is a Querydsl query type for MemberCareer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberCareer extends EntityPathBase<MemberCareer> {

    private static final long serialVersionUID = 934070828L;

    public static final QMemberCareer memberCareer = new QMemberCareer("memberCareer");

    public final StringPath companyName = createString("companyName");

    public final StringPath endDate = createString("endDate");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isCurrent = createBoolean("isCurrent");

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final StringPath startDate = createString("startDate");

    public final StringPath title = createString("title");

    public QMemberCareer(String variable) {
        super(MemberCareer.class, forVariable(variable));
    }

    public QMemberCareer(Path<? extends MemberCareer> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMemberCareer(PathMetadata metadata) {
        super(MemberCareer.class, metadata);
    }

}

