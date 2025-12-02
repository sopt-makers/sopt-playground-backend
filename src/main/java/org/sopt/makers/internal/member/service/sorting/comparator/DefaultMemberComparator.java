package org.sopt.makers.internal.member.service.sorting.comparator;

import java.util.Map;

import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.service.sorting.strategy.ProfileWeightStrategy;
import org.springframework.stereotype.Component;

/**
 * 기본 멤버 정렬
 * 정렬 순서:
 * 1. 최신 기수
 * 2. 프로필 정보 가중치
 * 3. 이름 ㄱㄴㄷ 순
 */
@Component
public class DefaultMemberComparator implements MemberSortingComparator {

	@Override
	public int compare(InternalUserDetails a, InternalUserDetails b,
	                   Map<Long, Member> memberMap,
	                   ProfileWeightStrategy weightStrategy) {

		// 1순위: 최신 기수 비교 (내림차순)
		int generationCompare = Integer.compare(b.lastGeneration(), a.lastGeneration());
		if (generationCompare != 0) {
			return generationCompare;
		}

		// 2순위: 프로필 정보 가중치 비교 (내림차순)
		Member memberA = memberMap.get(a.userId());
		Member memberB = memberMap.get(b.userId());
		int weightA = weightStrategy.calculate(a, memberA);
		int weightB = weightStrategy.calculate(b, memberB);
		int weightCompare = Integer.compare(weightB, weightA);
		if (weightCompare != 0) {
			return weightCompare;
		}

		// 3순위: 이름 ㄱㄴㄷ 순 비교 (오름차순)
		return a.name().compareTo(b.name());
	}
}
