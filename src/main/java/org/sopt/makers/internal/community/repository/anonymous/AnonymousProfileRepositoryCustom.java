package org.sopt.makers.internal.community.repository.anonymous;

import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;

import java.util.List;

public interface AnonymousProfileRepositoryCustom {

	// 최근 N개의 익명 프로필 조회 (닉네임 중복 제외용)
	List<AnonymousProfile> findTopByOrderByIdDescWithLimit(int limit);
}
