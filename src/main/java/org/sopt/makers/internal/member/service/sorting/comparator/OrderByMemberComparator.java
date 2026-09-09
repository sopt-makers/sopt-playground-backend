package org.sopt.makers.internal.member.service.sorting.comparator;

import java.util.Map;

import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.member.dto.profile.MemberProfileSummaryVo;
import org.sopt.makers.internal.member.domain.enums.OrderByCondition;
import org.sopt.makers.internal.member.service.sorting.strategy.ProfileWeightStrategy;
import org.springframework.stereotype.Component;

/**
 * OrderBy 멤버 정렬
 */
@Component
public class OrderByMemberComparator implements MemberSortingComparator {

	private final OrderByCondition orderByCondition;

	public OrderByMemberComparator() {
		this.orderByCondition = null;
	}

	private OrderByMemberComparator(OrderByCondition orderByCondition) {
		this.orderByCondition = orderByCondition;
	}

	public OrderByMemberComparator withOrderBy(OrderByCondition orderBy) {
		return new OrderByMemberComparator(orderBy);
	}

	@Override
	public int compare(InternalUserDetails a, InternalUserDetails b,
	                   Map<Long, MemberProfileSummaryVo> memberMap,
	                   ProfileWeightStrategy weightStrategy) {
		if (orderByCondition == null) {
			throw new IllegalStateException("OrderByCondition이 설정되지 않았습니다.");
		}

		return switch (orderByCondition) {
			case LATEST_REGISTERED -> compareLatestRegistered(a, b, memberMap);
			case OLDEST_REGISTERED -> compareOldestRegistered(a, b, memberMap);
			case LATEST_GENERATION -> compareLatestGeneration(a, b, memberMap, weightStrategy);
			case OLDEST_GENERATION -> compareOldestGeneration(a, b, memberMap, weightStrategy);
		};
	}

	private int compareLatestRegistered(InternalUserDetails a, InternalUserDetails b,
	                                    Map<Long, MemberProfileSummaryVo> memberMap) {
		MemberProfileSummaryVo memberA = memberMap.get(a.userId());
		MemberProfileSummaryVo memberB = memberMap.get(b.userId());

		if (memberA == null && memberB == null) return 0;
		if (memberA == null) return 1;
		if (memberB == null) return -1;

		return Long.compare(memberB.id(), memberA.id());
	}

	private int compareOldestRegistered(InternalUserDetails a, InternalUserDetails b,
	                                    Map<Long, MemberProfileSummaryVo> memberMap) {
		MemberProfileSummaryVo memberA = memberMap.get(a.userId());
		MemberProfileSummaryVo memberB = memberMap.get(b.userId());

		if (memberA == null && memberB == null) return 0;
		if (memberA == null) return 1;
		if (memberB == null) return -1;

		return Long.compare(memberA.id(), memberB.id());
	}

	private int compareLatestGeneration(InternalUserDetails a, InternalUserDetails b,
	                                    Map<Long, MemberProfileSummaryVo> memberMap,
	                                    ProfileWeightStrategy weightStrategy) {
		// 1순위: 최신 기수
		int generationCompare = Integer.compare(b.lastGeneration(), a.lastGeneration());
		if (generationCompare != 0) return generationCompare;

		// 2순위: 프로필 가중치
		MemberProfileSummaryVo memberA = memberMap.get(a.userId());
		MemberProfileSummaryVo memberB = memberMap.get(b.userId());
		int weightA = weightStrategy.calculate(a, memberA);
		int weightB = weightStrategy.calculate(b, memberB);
		int weightCompare = Integer.compare(weightB, weightA);
		if (weightCompare != 0) return weightCompare;

		// 3순위: 이름
		return a.name().compareTo(b.name());
	}

	private int compareOldestGeneration(InternalUserDetails a, InternalUserDetails b,
	                                    Map<Long, MemberProfileSummaryVo> memberMap,
	                                    ProfileWeightStrategy weightStrategy) {
		// 1순위: 오래된 기수
		int generationCompare = Integer.compare(a.lastGeneration(), b.lastGeneration());
		if (generationCompare != 0) return generationCompare;

		// 2순위: 프로필 가중치
		MemberProfileSummaryVo memberA = memberMap.get(a.userId());
		MemberProfileSummaryVo memberB = memberMap.get(b.userId());
		int weightA = weightStrategy.calculate(a, memberA);
		int weightB = weightStrategy.calculate(b, memberB);
		int weightCompare = Integer.compare(weightB, weightA);
		if (weightCompare != 0) return weightCompare;

		// 3순위: 이름
		return a.name().compareTo(b.name());
	}
}
