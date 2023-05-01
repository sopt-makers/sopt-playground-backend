package org.sopt.makers.internal.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = 551246094L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMember member = new QMember("member1");

    public final ListPath<MemberSoptActivity, QMemberSoptActivity> activities = this.<MemberSoptActivity, QMemberSoptActivity>createList("activities", MemberSoptActivity.class, QMemberSoptActivity.class, PathInits.DIRECT2);

    public final StringPath address = createString("address");

    public final BooleanPath allowOfficial = createBoolean("allowOfficial");

    public final StringPath authUserId = createString("authUserId");

    public final DatePath<java.time.LocalDate> birthday = createDate("birthday", java.time.LocalDate.class);

    public final ListPath<MemberCareer, QMemberCareer> careers = this.<MemberCareer, QMemberCareer>createList("careers", MemberCareer.class, QMemberCareer.class, PathInits.DIRECT2);

    public final StringPath email = createString("email");

    public final NumberPath<Integer> generation = createNumber("generation", Integer.class);

    public final BooleanPath hasProfile = createBoolean("hasProfile");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath idealType = createString("idealType");

    public final StringPath idpType = createString("idpType");

    public final StringPath interest = createString("interest");

    public final StringPath introduction = createString("introduction");

    public final ListPath<MemberLink, QMemberLink> links = this.<MemberLink, QMemberLink>createList("links", MemberLink.class, QMemberLink.class, PathInits.DIRECT2);

    public final StringPath major = createString("major");

    public final StringPath mbti = createString("mbti");

    public final StringPath mbtiDescription = createString("mbtiDescription");

    public final StringPath name = createString("name");

    public final BooleanPath openToSideProject = createBoolean("openToSideProject");

    public final BooleanPath openToWork = createBoolean("openToWork");

    public final StringPath phone = createString("phone");

    public final StringPath profileImage = createString("profileImage");

    public final StringPath selfIntroduction = createString("selfIntroduction");

    public final StringPath skill = createString("skill");

    public final NumberPath<Double> sojuCapacity = createNumber("sojuCapacity", Double.class);

    public final StringPath university = createString("university");

    public final QUserFavor userFavor;

    public QMember(String variable) {
        this(Member.class, forVariable(variable), INITS);
    }

    public QMember(Path<? extends Member> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMember(PathMetadata metadata, PathInits inits) {
        this(Member.class, metadata, inits);
    }

    public QMember(Class<? extends Member> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.userFavor = inits.isInitialized("userFavor") ? new QUserFavor(forProperty("userFavor")) : null;
    }

}

