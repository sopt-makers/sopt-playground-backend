package org.sopt.makers.internal.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QProjectLink is a Querydsl query type for ProjectLink
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProjectLink extends EntityPathBase<ProjectLink> {

    private static final long serialVersionUID = -1574873921L;

    public static final QProjectLink projectLink = new QProjectLink("projectLink");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final StringPath title = createString("title");

    public final StringPath url = createString("url");

    public QProjectLink(String variable) {
        super(ProjectLink.class, forVariable(variable));
    }

    public QProjectLink(Path<? extends ProjectLink> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProjectLink(PathMetadata metadata) {
        super(ProjectLink.class, metadata);
    }

}

