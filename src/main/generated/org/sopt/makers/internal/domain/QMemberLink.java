package org.sopt.makers.internal.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMemberLink is a Querydsl query type for MemberLink
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberLink extends EntityPathBase<MemberLink> {

    private static final long serialVersionUID = 577783336L;

    public static final QMemberLink memberLink = new QMemberLink("memberLink");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final StringPath title = createString("title");

    public final StringPath url = createString("url");

    public QMemberLink(String variable) {
        super(MemberLink.class, forVariable(variable));
    }

    public QMemberLink(Path<? extends MemberLink> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMemberLink(PathMetadata metadata) {
        super(MemberLink.class, metadata);
    }

}

