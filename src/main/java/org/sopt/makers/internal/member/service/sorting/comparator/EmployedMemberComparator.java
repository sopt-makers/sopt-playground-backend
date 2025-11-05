package org.sopt.makers.internal.member.service.sorting.comparator;

import java.util.Map;

import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.service.sorting.strategy.ProfileWeightStrategy;
import org.springframework.stereotype.Component;

/**
 * 재직중 필터용 멤버 정렬 비교자
 * 정렬 순서:
 * 1. 프로필 정보 가중치 (내림차순) - 커리어 중심 가중치 적용
 * 2. 이름 ㄱㄴㄷ 순 (오름차순)
 *
 * 주의: 최신 기수 정렬은 제거됨
 */
@Component
public class EmployedMemberComparator implements MemberSortingComparator {

	@Override
	public int compare(InternalUserDetails a, InternalUserDetails b,
	                   Map<Long, Member> memberMap,
	                   ProfileWeightStrategy weightStrategy) {
		// 1순위: 프로필 정보 가중치 비교 (내림차순)
		Member memberA = memberMap.get(a.userId());
		Member memberB = memberMap.get(b.userId());
		int weightA = weightStrategy.calculate(a, memberA);
		int weightB = weightStrategy.calculate(b, memberB);
		int weightCompare = Integer.compare(weightB, weightA);
		if (weightCompare != 0) {
			return weightCompare;
		}

		// 2순위: 이름 ㄱㄴㄷ 순 비교 (오름차순)
		return a.name().compareTo(b.name());
	}
}
