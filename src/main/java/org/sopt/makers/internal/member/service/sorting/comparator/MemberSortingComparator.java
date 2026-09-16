package org.sopt.makers.internal.member.service.sorting.comparator;

import java.util.Map;

import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.member.dto.profile.MemberProfileSummaryVo;
import org.sopt.makers.internal.member.service.sorting.strategy.ProfileWeightStrategy;

/**
 * 멤버 정렬 비교 전략 인터페이스
 * 필터 조건에 따라 다른 정렬 방식 제공
 */
public interface MemberSortingComparator {
	int compare(InternalUserDetails a, InternalUserDetails b,
	            Map<Long, MemberProfileSummaryVo> memberMap,
	            ProfileWeightStrategy weightStrategy);
}
