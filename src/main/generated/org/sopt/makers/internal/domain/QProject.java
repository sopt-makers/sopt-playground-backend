package org.sopt.makers.internal.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QProject is a Querydsl query type for Project
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProject extends EntityPathBase<Project> {

    private static final long serialVersionUID = -1349432539L;

    public static final QProject project = new QProject("project");

    public final StringPath category = createString("category");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath detail = createString("detail");

    public final DatePath<java.time.LocalDate> endAt = createDate("endAt", java.time.LocalDate.class);

    public final NumberPath<Integer> generation = createNumber("generation", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ArrayPath<String[], String> images = createArray("images", String[].class);

    public final BooleanPath isAvailable = createBoolean("isAvailable");

    public final BooleanPath isFounding = createBoolean("isFounding");

    public final StringPath logoImage = createString("logoImage");

    public final StringPath name = createString("name");

    public final ArrayPath<String[], String> serviceType = createArray("serviceType", String[].class);

    public final DatePath<java.time.LocalDate> startAt = createDate("startAt", java.time.LocalDate.class);

    public final StringPath summary = createString("summary");

    public final StringPath thumbnailImage = createString("thumbnailImage");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> writerId = createNumber("writerId", Long.class);

    public QProject(String variable) {
        super(Project.class, forVariable(variable));
    }

    public QProject(Path<? extends Project> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProject(PathMetadata metadata) {
        super(Project.class, metadata);
    }

}

