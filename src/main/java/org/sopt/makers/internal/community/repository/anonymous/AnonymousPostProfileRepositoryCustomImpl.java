package org.sopt.makers.internal.community.repository.anonymous;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousPostProfile;
import org.sopt.makers.internal.domain.community.QAnonymousPostProfile;

import java.util.List;

@RequiredArgsConstructor
public class AnonymousPostProfileRepositoryCustomImpl implements AnonymousPostProfileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<AnonymousPostProfile> findTopByOrderByIdDescWithLimit(int limit) {

        QAnonymousPostProfile anonymousPostProfile = QAnonymousPostProfile.anonymousPostProfile;

        return queryFactory
                .selectFrom(anonymousPostProfile)
                .orderBy(anonymousPostProfile.createdAt.desc())
                .limit(limit)
                .fetch();
    }
}
