package org.sopt.makers.internal.community.repository.anonymous;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;
import org.sopt.makers.internal.community.domain.anonymous.QAnonymousProfile;

import java.util.List;

@RequiredArgsConstructor
public class AnonymousProfileRepositoryCustomImpl implements AnonymousProfileRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<AnonymousProfile> findTopByOrderByIdDescWithLimit(int limit) {

		QAnonymousProfile anonymousProfile = QAnonymousProfile.anonymousProfile;

		return queryFactory
				.selectFrom(anonymousProfile)
				.orderBy(anonymousProfile.createdAt.desc())
				.limit(limit)
				.fetch();
	}
}
