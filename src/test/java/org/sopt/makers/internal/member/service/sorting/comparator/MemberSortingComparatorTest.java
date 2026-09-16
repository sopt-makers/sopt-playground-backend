package org.sopt.makers.internal.member.service.sorting.comparator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.member.domain.enums.OrderByCondition;
import org.sopt.makers.internal.member.dto.profile.MemberActivityCountVo;
import org.sopt.makers.internal.member.dto.profile.MemberBasicInfoVo;
import org.sopt.makers.internal.member.dto.profile.MemberFavorVo;
import org.sopt.makers.internal.member.dto.profile.MemberIntroVo;
import org.sopt.makers.internal.member.dto.profile.MemberPersonalityVo;
import org.sopt.makers.internal.member.dto.profile.MemberProfileSummaryVo;
import org.sopt.makers.internal.member.service.sorting.strategy.DefaultProfileWeightStrategy;
import org.sopt.makers.internal.member.service.sorting.strategy.ProfileWeightStrategy;

/**
 * 정렬 비교자 테스트.
 *
 * <p>비교자 자체는 이번에 제네릭 타입만 바뀌었지만, <b>가중치의 출처가 엔티티에서 VO 로</b>
 * 바뀌었다. 배선이 어긋나면 순서가 조용히 달라지므로 각 비교자의 tie-break 사슬을 고정한다.
 */
class MemberSortingComparatorTest {

	private final ProfileWeightStrategy weightStrategy = new DefaultProfileWeightStrategy();

	@Nested
	@DisplayName("DefaultMemberComparator — 기수 → 가중치 → 이름")
	class Default {

		private final DefaultMemberComparator comparator = new DefaultMemberComparator();

		@Test
		@DisplayName("기수가 높은 멤버가 먼저 온다")
		void 기수_내림차순() {
			InternalUserDetails older = user(1L, "가나다", 35);
			InternalUserDetails newer = user(2L, "하하하", 38);

			assertThat(sortIds(comparator, List.of(older, newer), weights(1L, 0, 2L, 0)))
				.containsExactly(2L, 1L);
		}

		@Test
		@DisplayName("같은 기수면 가중치가 높은 멤버가 먼저 온다")
		void 동기수면_가중치() {
			InternalUserDetails low = user(1L, "가나다", 38);
			InternalUserDetails high = user(2L, "하하하", 38);

			// 링크 개수로 가중치 차이를 만든다 (링크 1개당 1점)
			assertThat(sortIds(comparator, List.of(low, high), weights(1L, 0, 2L, 5)))
				.containsExactly(2L, 1L);
		}

		@Test
		@DisplayName("기수와 가중치가 같으면 이름 오름차순이다")
		void 동점이면_이름순() {
			InternalUserDetails a = user(1L, "하하하", 38);
			InternalUserDetails b = user(2L, "가나다", 38);

			assertThat(sortIds(comparator, List.of(a, b), weights(1L, 3, 2L, 3)))
				.containsExactly(2L, 1L);
		}
	}

	@Nested
	@DisplayName("EmployedMemberComparator — 가중치 → 이름 (기수 무시)")
	class Employed {

		private final EmployedMemberComparator comparator = new EmployedMemberComparator();

		@Test
		@DisplayName("기수가 낮아도 가중치가 높으면 먼저 온다")
		void 기수를_보지_않는다() {
			InternalUserDetails newerButEmpty = user(1L, "가나다", 38);
			InternalUserDetails olderButRich = user(2L, "하하하", 30);

			assertThat(sortIds(comparator, List.of(newerButEmpty, olderButRich), weights(1L, 0, 2L, 5)))
				.containsExactly(2L, 1L);
		}

		@Test
		@DisplayName("가중치가 같으면 이름 오름차순이다")
		void 동점이면_이름순() {
			InternalUserDetails a = user(1L, "하하하", 38);
			InternalUserDetails b = user(2L, "가나다", 30);

			assertThat(sortIds(comparator, List.of(a, b), weights(1L, 2, 2L, 2)))
				.containsExactly(2L, 1L);
		}
	}

	@Nested
	@DisplayName("OrderByMemberComparator")
	class OrderBy {

		@Test
		@DisplayName("LATEST_REGISTERED 는 id 내림차순이며 가중치를 보지 않는다")
		void 최근_등록순() {
			InternalUserDetails older = user(1L, "가나다", 30);
			InternalUserDetails newer = user(2L, "하하하", 38);

			// 가중치를 일부러 반대로 줘도 id 순서만 따라야 한다
			assertThat(sortBy(OrderByCondition.LATEST_REGISTERED, List.of(older, newer), weights(1L, 99, 2L, 0)))
				.containsExactly(2L, 1L);
		}

		@Test
		@DisplayName("OLDEST_REGISTERED 는 id 오름차순이며 가중치를 보지 않는다")
		void 예전_등록순() {
			InternalUserDetails older = user(1L, "가나다", 30);
			InternalUserDetails newer = user(2L, "하하하", 38);

			assertThat(sortBy(OrderByCondition.OLDEST_REGISTERED, List.of(newer, older), weights(1L, 0, 2L, 99)))
				.containsExactly(1L, 2L);
		}

		@Test
		@DisplayName("LATEST_GENERATION 은 기수 내림차순 후 가중치를 본다")
		void 최근_기수순() {
			InternalUserDetails a = user(1L, "가나다", 38);
			InternalUserDetails b = user(2L, "하하하", 38);
			InternalUserDetails c = user(3L, "다다다", 30);

			assertThat(sortBy(OrderByCondition.LATEST_GENERATION, List.of(c, a, b), weights(1L, 0, 2L, 5, 3L, 99)))
				.containsExactly(2L, 1L, 3L);
		}

		@Test
		@DisplayName("OLDEST_GENERATION 은 기수 오름차순 후 가중치를 본다")
		void 예전_기수순() {
			InternalUserDetails a = user(1L, "가나다", 38);
			InternalUserDetails c = user(3L, "다다다", 30);

			assertThat(sortBy(OrderByCondition.OLDEST_GENERATION, List.of(a, c), weights(1L, 99, 3L, 0)))
				.containsExactly(3L, 1L);
		}

		private List<Long> sortBy(
			OrderByCondition condition,
			List<InternalUserDetails> users,
			Map<Long, MemberProfileSummaryVo> summaryMap
		) {
			OrderByMemberComparator base = new OrderByMemberComparator().withOrderBy(condition);
			return users.stream()
				.sorted((a, b) -> base.compare(a, b, summaryMap, weightStrategy))
				.map(InternalUserDetails::userId)
				.collect(Collectors.toList());
		}
	}

	@Nested
	@DisplayName("TeamActivityMemberComparator — 해당 팀 최근 기수 → 가중치 → 이름")
	class TeamActivity {

		private final TeamActivityMemberComparator comparator =
			new TeamActivityMemberComparator().withTeam("운영팀");

		@Test
		@DisplayName("해당 팀으로 활동한 기수가 최근인 멤버가 먼저 온다")
		void 팀_활동_기수_내림차순() {
			InternalUserDetails old = userWithTeam(1L, "가나다", 30, "운영팀", 30);
			InternalUserDetails recent = userWithTeam(2L, "하하하", 38, "운영팀", 38);

			assertThat(sortIds(comparator, List.of(old, recent), weights(1L, 0, 2L, 0)))
				.containsExactly(2L, 1L);
		}

		@Test
		@DisplayName("해당 팀 기록이 없는 멤버는 뒤로 밀린다")
		void 팀_기록_없으면_뒤로() {
			InternalUserDetails noTeam = userWithTeam(1L, "가나다", 38, "미디어팀", 38);
			InternalUserDetails hasTeam = userWithTeam(2L, "하하하", 30, "운영팀", 30);

			assertThat(sortIds(comparator, List.of(noTeam, hasTeam), weights(1L, 99, 2L, 0)))
				.containsExactly(2L, 1L);
		}
	}

	// --- 헬퍼 -------------------------------------------------------------

	/** (id, 링크개수) 쌍을 받아 요약 맵을 만든다. 링크 1개 = 가중치 1점. */
	private Map<Long, MemberProfileSummaryVo> weights(Object... idAndLinkCount) {
		Map<Long, MemberProfileSummaryVo> map = new HashMap<>();
		for (int i = 0; i < idAndLinkCount.length; i += 2) {
			Long id = (Long) idAndLinkCount[i];
			int linkCount = (Integer) idAndLinkCount[i + 1];
			map.put(id, summary(id, linkCount));
		}
		return map;
	}

	/** 링크 개수만으로 가중치를 조절한다 (링크 1개 = 1점). */
	private MemberProfileSummaryVo summary(Long id, int linkCount) {
		return new MemberProfileSummaryVo(
			id,
			new MemberBasicInfoVo(null, null, null),
			new MemberIntroVo(null, null, null),
			new MemberPersonalityVo(null, null, null, null, null),
			new MemberFavorVo(null, null, null, null, null, null),
			new MemberActivityCountVo(linkCount, 0, List.of())
		);
	}

	private List<Long> sortIds(
		MemberSortingComparator comparator,
		List<InternalUserDetails> users,
		Map<Long, MemberProfileSummaryVo> summaryMap
	) {
		return users.stream()
			.sorted((a, b) -> comparator.compare(a, b, summaryMap, weightStrategy))
			.map(InternalUserDetails::userId)
			.collect(Collectors.toList());
	}

	private InternalUserDetails user(Long id, String name, int lastGeneration) {
		return new InternalUserDetails(id, name, null, null, null, null, lastGeneration, List.of());
	}

	private InternalUserDetails userWithTeam(Long id, String name, int lastGeneration, String team, int teamGeneration) {
		SoptActivity activity = new SoptActivity(1, teamGeneration, "SERVER", team, "MEMBER", true);
		return new InternalUserDetails(id, name, null, null, null, null, lastGeneration, List.of(activity));
	}
}
