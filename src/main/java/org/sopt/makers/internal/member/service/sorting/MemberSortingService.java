package org.sopt.makers.internal.member.service.sorting;

import java.util.Comparator;
import java.util.Map;

import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.enums.OrderByCondition;
import org.sopt.makers.internal.member.service.sorting.comparator.DefaultMemberComparator;
import org.sopt.makers.internal.member.service.sorting.comparator.EmployedMemberComparator;
import org.sopt.makers.internal.member.service.sorting.comparator.MemberSortingComparator;
import org.sopt.makers.internal.member.service.sorting.comparator.OrderByMemberComparator;
import org.sopt.makers.internal.member.service.sorting.comparator.TeamActivityMemberComparator;
import org.sopt.makers.internal.member.service.sorting.strategy.DefaultProfileWeightStrategy;
import org.sopt.makers.internal.member.service.sorting.strategy.EmployedProfileWeightStrategy;
import org.sopt.makers.internal.member.service.sorting.strategy.ProfileWeightStrategy;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * 멤버 정렬 서비스
 * 필터 조건에 따라 적절한 정렬 전략을 선택하고 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class MemberSortingService {

	private final DefaultProfileWeightStrategy defaultWeightStrategy;
	private final EmployedProfileWeightStrategy employedWeightStrategy;
	private final DefaultMemberComparator defaultComparator;
	private final EmployedMemberComparator employedComparator;
	private final TeamActivityMemberComparator teamActivityComparator;
	private final OrderByMemberComparator orderByMemberComparator;

	/**
	 * 필터 조건에 맞는 Comparator 생성
	 *
	 * @param memberMap 멤버 ID와 Member 엔티티의 매핑
	 * @param employed  재직중 필터
	 * @param team      활동 팀 필터 (운영팀, 미디어팀, 임원진 등)
	 * @return 조건에 맞게 결정된 Comparator
	 */
	public Comparator<InternalUserDetails> createComparator(
		Map<Long, Member> memberMap,
		Integer employed,
		String team) {

		// 1. 가중치 전략 선택
		ProfileWeightStrategy weightStrategy = selectWeightStrategy(employed);

		// 2. 정렬 방식 선택
		MemberSortingComparator sortingComparator = selectSortingComparator(employed, team);

		// 3. Comparator 생성
		return (a, b) -> sortingComparator.compare(a, b, memberMap, weightStrategy);
	}

	/**
	 * 가중치 전략 선택
	 */
	private ProfileWeightStrategy selectWeightStrategy(Integer employed) {
		// 재직중 필터가 있으면 재직중 가중치 전략 사용
		if (employed != null && employed == 1) {
			return employedWeightStrategy;
		}

		return defaultWeightStrategy;
	}

	/**
	 * 필터 조건에 따라 정렬 방식.
	 * 우선순위:
	 * 1. 팀 필터 → TeamActivityMemberComparator
	 * 2. 재직중 필터 → EmployedMemberComparator
	 * 3. 그 외 → DefaultMemberComparator
	 *
	 * @param employed 재직중 필터 (1: 재직중)
	 * @param team     운팀/미팀 필터
	 * @return 선택된 정렬 비교자
	 */
	private MemberSortingComparator selectSortingComparator(Integer employed, String team) {
		// 1순위: 운팀/미팀 필터
		if (isTeamActivityFilter(team)) {
			return teamActivityComparator.withTeam(team);
		}

		// 2순위: 재직중 필터
		if (employed != null && employed == 1) {
			return employedComparator;
		}

		// 3순위: 기본 정렬 (최신 기수 → 가중치 → 이름)
		return defaultComparator;
	}

	/**
	 * 팀 활동 필터여부 확인 => 운팀/미팀의 경우에만 다른 정렬 로직 적용
	 */
	private boolean isTeamActivityFilter(String team) {
		return "운영팀".equals(team) || "미디어팀".equals(team);
	}

	/**
	 * orderBy 파라미터가 있으면 필터별 정렬 정책보다 우선적으로 처리
	 */
	public Comparator<InternalUserDetails> createComparatorByOrderCondition(
		Map<Long, Member> memberMap,
		OrderByCondition orderBy,
		Integer employed) {

		if (orderBy == null) {
			throw new IllegalArgumentException("OrderByCondition은 null일 수 없습니다.");
		}

		// 가중치 전략 선택 (재직중 여부)
		ProfileWeightStrategy weightStrategy = selectWeightStrategy(employed);

		return (a, b) -> orderByMemberComparator.withOrderBy(orderBy)
			.compare(a, b, memberMap, weightStrategy);
	}
}
