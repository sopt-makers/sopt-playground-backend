package org.sopt.makers.internal.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserFavor is a Querydsl query type for UserFavor
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QUserFavor extends BeanPath<UserFavor> {

    private static final long serialVersionUID = 1486173759L;

    public static final QUserFavor userFavor = new QUserFavor("userFavor");

    public final BooleanPath isHardPeachLover = createBoolean("isHardPeachLover");

    public final BooleanPath isMintChocoLover = createBoolean("isMintChocoLover");

    public final BooleanPath isPourSauceLover = createBoolean("isPourSauceLover");

    public final BooleanPath isRedBeanFishBreadLover = createBoolean("isRedBeanFishBreadLover");

    public final BooleanPath isRiceTteokLover = createBoolean("isRiceTteokLover");

    public final BooleanPath isSojuLover = createBoolean("isSojuLover");

    public QUserFavor(String variable) {
        super(UserFavor.class, forVariable(variable));
    }

    public QUserFavor(Path<? extends UserFavor> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserFavor(PathMetadata metadata) {
        super(UserFavor.class, metadata);
    }

}

