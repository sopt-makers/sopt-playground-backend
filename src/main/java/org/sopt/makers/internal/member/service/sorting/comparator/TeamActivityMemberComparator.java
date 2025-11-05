package org.sopt.makers.internal.member.service.sorting.comparator;

import java.util.Comparator;
import java.util.Map;

import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.service.sorting.strategy.ProfileWeightStrategy;
import org.springframework.stereotype.Component;

/**
 * 팀 활동(운팀/미팀) 필터용 멤버 정렬
 * 정렬 순서:
 * 1. 가장 최근 해당 팀에 속해있던 기수 (내림차순)
 * 2. 프로필 정보 가중치 (내림차순)
 * 3. 이름 ㄱㄴㄷ 순 (오름차순)
 */
@Component
public class TeamActivityMemberComparator implements MemberSortingComparator {

	private final String targetTeam;

	public TeamActivityMemberComparator(String targetTeam) {
		this.targetTeam = targetTeam;
	}

	public TeamActivityMemberComparator() {
		this.targetTeam = null;
	}

	public TeamActivityMemberComparator withTeam(String team) {
		return new TeamActivityMemberComparator(team);
	}

	@Override
	public int compare(InternalUserDetails a, InternalUserDetails b,
	                   Map<Long, Member> memberMap,
	                   ProfileWeightStrategy weightStrategy) {

		// 1순위: 가장 최근 해당 팀 소속 기수 비교
		Integer latestTeamGenerationA = getLatestTeamGeneration(a);
		Integer latestTeamGenerationB = getLatestTeamGeneration(b);

		if (latestTeamGenerationA == null && latestTeamGenerationB == null) {
			// 둘 다 해당 팀 기록이 없으면 일반 기수로 비교
			int generationCompare = Integer.compare(b.lastGeneration(), a.lastGeneration());
			if (generationCompare != 0) {
				return generationCompare;
			}
		} else if (latestTeamGenerationA == null) {
			return 1;
		} else if (latestTeamGenerationB == null) {
			return -1;
		} else {
			// 둘 다 해당 팀 기록이 있으면 최근 팀 소속 기수로 비교
			int teamGenerationCompare = Integer.compare(latestTeamGenerationB, latestTeamGenerationA);
			if (teamGenerationCompare != 0) {
				return teamGenerationCompare;
			}
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

	/**
	 * 팀에 속했던 활동 중 가장 최근 기수 반환
	 */
	private Integer getLatestTeamGeneration(InternalUserDetails userDetails) {
		if (targetTeam == null || userDetails.soptActivities() == null) {
			return null;
		}

		return userDetails.soptActivities().stream()
			.filter(activity -> targetTeam.equals(activity.team()))
			.map(SoptActivity::generation)
			.max(Comparator.naturalOrder())
			.orElse(null);
	}
}
