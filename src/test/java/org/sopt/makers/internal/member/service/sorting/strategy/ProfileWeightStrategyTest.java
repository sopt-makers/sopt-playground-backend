package org.sopt.makers.internal.member.service.sorting.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.member.dto.profile.MemberActivityCountVo;
import org.sopt.makers.internal.member.dto.profile.MemberBasicInfoVo;
import org.sopt.makers.internal.member.dto.profile.MemberFavorVo;
import org.sopt.makers.internal.member.dto.profile.MemberIntroVo;
import org.sopt.makers.internal.member.dto.profile.MemberPersonalityVo;
import org.sopt.makers.internal.member.dto.profile.MemberProfileSummaryVo;

/**
 * 프로필 가중치 계산 테스트.
 *
 * <p>가중치가 1점만 달라져도 목록 정렬 순서가 바뀐다. 응답 필드는 전부 정확하므로
 * 눈으로는 드러나지 않는다. 그래서 기대값을 손으로 계산해 하드코딩한다.
 *
 * <p>기대값은 아래 배점표에서 나온다.
 * <pre>
 *                        Default   Employed
 *   profileImage            5          3
 *   introduction            3          3
 *   career (개당)            3          5
 *   link (개당)              1          1
 *   그 외 항목 (각각)          1          1
 * </pre>
 */
class ProfileWeightStrategyTest {

	private final DefaultProfileWeightStrategy defaultStrategy = new DefaultProfileWeightStrategy();
	private final EmployedProfileWeightStrategy employedStrategy = new EmployedProfileWeightStrategy();

	@Test
	@DisplayName("모든 항목이 채워진 경우")
	void 전체_채움() {
		// 플랫폼   : image 5 + birthday 1 + phone 1 + email 1        = 8 (Employed 는 image 3 → 6)
		// 소개     : introduction 3 + selfIntroduction 1 + skill 1   = 5
		// 기본     : address 1 + university 1 + major 1              = 3
		// 성향     : mbti/설명/주량/관심사/이상형 각 1                  = 5
		// 취향     : 6개 응답                                         = 6
		// 활동     : link 2개 × 1                                    = 2
		//           career 1개 × 3(Default) / × 5(Employed)         = 3 / 5
		InternalUserDetails userDetails = userDetails("img.png", "1999-01-01", "01012345678", "a@b.com");
		MemberProfileSummaryVo summary = new MemberProfileSummaryVo(
			1L,
			new MemberBasicInfoVo("서울시 강남구", "서울대학교", "컴퓨터공학과"),
			new MemberIntroVo("소개", "자기소개", "Java"),
			new MemberPersonalityVo("INTJ", "설명", 1.5, "관심사", "이상형"),
			new MemberFavorVo(true, false, true, false, true, false),
			new MemberActivityCountVo(2, 1, List.of("토스"))
		);

		assertThat(defaultStrategy.calculate(userDetails, summary)).isEqualTo(32);
		assertThat(employedStrategy.calculate(userDetails, summary)).isEqualTo(32);
	}

	@Test
	@DisplayName("커리어가 많으면 두 전략의 점수가 갈린다")
	void 커리어가_많으면_전략별로_갈린다() {
		// Default  : image 5 + career 3개 × 3 = 5 + 9  = 14
		// Employed : image 3 + career 3개 × 5 = 3 + 15 = 18
		InternalUserDetails userDetails = userDetails("img.png", null, null, null);
		MemberProfileSummaryVo summary = summaryWithActivity(new MemberActivityCountVo(0, 3, List.of()));

		assertThat(defaultStrategy.calculate(userDetails, summary)).isEqualTo(14);
		assertThat(employedStrategy.calculate(userDetails, summary)).isEqualTo(18);
	}

	@Test
	@DisplayName("취향 6개가 전부 null 이면 0점 — 엔티티의 userFavor == null 과 같은 결과")
	void 취향_전부_null() {
		// 기본 university 1 + link 1개 × 1 = 2. 취향은 0점.
		InternalUserDetails userDetails = userDetails(null, null, null, null);
		MemberProfileSummaryVo summary = new MemberProfileSummaryVo(
			1L,
			new MemberBasicInfoVo(null, "연세대학교", null),
			new MemberIntroVo(null, null, null),
			new MemberPersonalityVo(null, null, null, null, null),
			new MemberFavorVo(null, null, null, null, null, null),
			new MemberActivityCountVo(1, 0, List.of())
		);

		assertThat(defaultStrategy.calculate(userDetails, summary)).isEqualTo(2);
		assertThat(employedStrategy.calculate(userDetails, summary)).isEqualTo(2);
	}

	@Test
	@DisplayName("취향이 false 여도 응답한 것이므로 점수에 반영된다")
	void 취향_false도_점수에_반영() {
		InternalUserDetails userDetails = userDetails(null, null, null, null);
		MemberProfileSummaryVo summary = new MemberProfileSummaryVo(
			1L,
			new MemberBasicInfoVo(null, null, null),
			new MemberIntroVo(null, null, null),
			new MemberPersonalityVo(null, null, null, null, null),
			new MemberFavorVo(false, false, false, null, null, null),
			MemberActivityCountVo.EMPTY
		);

		assertThat(defaultStrategy.calculate(userDetails, summary)).isEqualTo(3);
	}

	@Test
	@DisplayName("공백만 있는 문자열은 채워진 것으로 보지 않는다")
	void 공백_문자열은_점수에_반영되지_않는다() {
		// 플랫폼에서 email 1점만 유효. 로컬 필드는 전부 공백이라 0점.
		InternalUserDetails userDetails = userDetails("   ", "", null, "a@b.com");
		MemberProfileSummaryVo summary = new MemberProfileSummaryVo(
			1L,
			new MemberBasicInfoVo("   ", "", "  "),
			new MemberIntroVo("   ", "", "  "),
			new MemberPersonalityVo("  ", "", null, "   ", ""),
			new MemberFavorVo(null, null, null, null, null, null),
			MemberActivityCountVo.EMPTY
		);

		assertThat(defaultStrategy.calculate(userDetails, summary)).isEqualTo(1);
		assertThat(employedStrategy.calculate(userDetails, summary)).isEqualTo(1);
	}

	@Test
	@DisplayName("주량은 0.0 이어도 응답한 값이므로 1점이다")
	void 주량_0점0도_점수에_반영() {
		InternalUserDetails userDetails = userDetails(null, null, null, null);
		MemberProfileSummaryVo summary = new MemberProfileSummaryVo(
			1L,
			new MemberBasicInfoVo(null, null, null),
			new MemberIntroVo(null, null, null),
			new MemberPersonalityVo(null, null, 0.0, null, null),
			new MemberFavorVo(null, null, null, null, null, null),
			MemberActivityCountVo.EMPTY
		);

		assertThat(defaultStrategy.calculate(userDetails, summary)).isEqualTo(1);
	}

	@Test
	@DisplayName("아무것도 채워지지 않으면 0점이다")
	void 전부_비어있음() {
		InternalUserDetails userDetails = userDetails(null, null, null, null);
		MemberProfileSummaryVo summary = summaryWithActivity(MemberActivityCountVo.EMPTY);

		assertThat(defaultStrategy.calculate(userDetails, summary)).isZero();
		assertThat(employedStrategy.calculate(userDetails, summary)).isZero();
	}

	@Test
	@DisplayName("로컬 프로필이 없는 유저는 플랫폼 점수만 받는다")
	void 로컬_프로필이_null() {
		// Default : image 5 + birthday 1 = 6
		// Employed: image 3 + birthday 1 = 4
		InternalUserDetails userDetails = userDetails("img.png", "1999-01-01", null, null);

		assertThat(defaultStrategy.calculate(userDetails, null)).isEqualTo(6);
		assertThat(employedStrategy.calculate(userDetails, null)).isEqualTo(4);
	}

	private InternalUserDetails userDetails(String profileImage, String birthday, String phone, String email) {
		return new InternalUserDetails(1L, "홍길동", profileImage, birthday, phone, email, 38, List.of());
	}

	private MemberProfileSummaryVo summaryWithActivity(MemberActivityCountVo activity) {
		return new MemberProfileSummaryVo(
			1L,
			new MemberBasicInfoVo(null, null, null),
			new MemberIntroVo(null, null, null),
			new MemberPersonalityVo(null, null, null, null, null),
			new MemberFavorVo(null, null, null, null, null, null),
			activity
		);
	}
}
