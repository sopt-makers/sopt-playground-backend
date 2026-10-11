package org.sopt.makers.internal.sopmodam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.sopt.makers.internal.exception.ForbiddenException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamMemberType;

/**
 * 솝모담 사용자 유형 판별과 권한 테스트.
 *
 * <p>기준 기수는 서버 기준 기수({@code Constant.CURRENT_GENERATION})가 아니라 <b>회차의 기수</b>다.
 * <pre>
 *   lastGeneration &lt; 회차 기수                                     → HONORARY
 *   lastGeneration &gt; 회차 기수                                     → NOT_TARGET
 *   lastGeneration = 회차 기수, 회차 기수 미만 SOPT 활동(isSopt) 있음 → ACTIVE_OB
 *   lastGeneration = 회차 기수, 그런 활동 없음                        → ACTIVE_YB
 * </pre>
 */
class SopmodamMemberPolicyTest {

	private static final int ROUND_GENERATION = 37;

	private final SopmodamMemberPolicy memberPolicy = new SopmodamMemberPolicy();

	@Nested
	@DisplayName("사용자 유형 판별 - 활동 YB / OB")
	class ActiveMember {

		@Test
		@DisplayName("회차 기수에만 활동했으면 YB")
		void 회차_기수만_활동하면_YB() {
			InternalUserDetails user = user(37, sopt(37));
			assertThat(memberPolicy.resolveType(user, ROUND_GENERATION)).isEqualTo(SopmodamMemberType.ACTIVE_YB);
		}

		@Test
		@DisplayName("이전 기수에 SOPT 활동이 있으면 OB")
		void 이전_기수_SOPT_활동이_있으면_OB() {
			InternalUserDetails user = user(37, sopt(33), sopt(37));
			assertThat(memberPolicy.resolveType(user, ROUND_GENERATION)).isEqualTo(SopmodamMemberType.ACTIVE_OB);
		}

		@Test
		@DisplayName("바로 직전 기수 활동도 이전 활동으로 본다")
		void 직전_기수_활동도_OB() {
			InternalUserDetails user = user(37, sopt(36), sopt(37));
			assertThat(memberPolicy.resolveType(user, ROUND_GENERATION)).isEqualTo(SopmodamMemberType.ACTIVE_OB);
		}

		@Test
		@DisplayName("이전 기수 활동이 메이커스뿐이면 YB (명세 §7.3 확인 필요 항목 — 현재 정책을 고정)")
		void 이전_활동이_메이커스뿐이면_YB() {
			InternalUserDetails user = user(37, makers(36), sopt(37));
			assertThat(memberPolicy.resolveType(user, ROUND_GENERATION)).isEqualTo(SopmodamMemberType.ACTIVE_YB);
		}

		@Test
		@DisplayName("메이커스 초기 기수 번호(1~4)를 SOPT 기수와 비교하지 않는다")
		void 메이커스_초기_기수_번호는_비교하지_않는다() {
			// 메이커스 2기(= SOPT 32기 시기)는 generation 값이 2 라 숫자만 보면 37 보다 작다
			InternalUserDetails user = user(37, makers(2), sopt(37));
			assertThat(memberPolicy.resolveType(user, ROUND_GENERATION)).isEqualTo(SopmodamMemberType.ACTIVE_YB);
		}

		@Test
		@DisplayName("같은 기수에 SOPT 와 메이커스를 함께 해도 YB")
		void 같은_기수_SOPT_메이커스_병행은_YB() {
			InternalUserDetails user = user(37, sopt(37), makers(37));
			assertThat(memberPolicy.resolveType(user, ROUND_GENERATION)).isEqualTo(SopmodamMemberType.ACTIVE_YB);
		}

		@Test
		@DisplayName("활동 이력이 비어 있어도 마지막 기수가 회차 기수면 YB")
		void 활동_이력이_비어있으면_YB() {
			InternalUserDetails user = user(37);
			assertThat(memberPolicy.resolveType(user, ROUND_GENERATION)).isEqualTo(SopmodamMemberType.ACTIVE_YB);
		}
	}

	@Nested
	@DisplayName("사용자 유형 판별 - 명예기수 / 대상 아님")
	class InactiveMember {

		@Test
		@DisplayName("마지막 기수가 회차 기수 바로 이전이면 명예기수")
		void 직전_기수까지_활동하면_명예기수() {
			InternalUserDetails user = user(36, sopt(36));
			assertThat(memberPolicy.resolveType(user, ROUND_GENERATION)).isEqualTo(SopmodamMemberType.HONORARY);
		}

		@Test
		@DisplayName("여러 기수를 활동했어도 마지막 기수가 이전이면 명예기수")
		void 여러_기수_활동_후_졸업하면_명예기수() {
			InternalUserDetails user = user(35, sopt(30), sopt(33), sopt(35));
			assertThat(memberPolicy.resolveType(user, ROUND_GENERATION)).isEqualTo(SopmodamMemberType.HONORARY);
		}

		@Test
		@DisplayName("마지막 기수가 회차 기수보다 뒤면 NOT_TARGET")
		void 회차_이후_기수는_NOT_TARGET() {
			InternalUserDetails user = user(38, sopt(38));
			assertThat(memberPolicy.resolveType(user, ROUND_GENERATION)).isEqualTo(SopmodamMemberType.NOT_TARGET);
		}

		@Test
		@DisplayName("회차 기수에 활동했더라도 이후 기수까지 이어서 활동했으면 NOT_TARGET")
		void 회차_기수_활동_후_이어서_활동하면_NOT_TARGET() {
			// lastGeneration 기준으로만 판단한다. 회차 기수 활동 여부는 보지 않는다
			InternalUserDetails user = user(38, sopt(37), sopt(38));
			assertThat(memberPolicy.resolveType(user, ROUND_GENERATION)).isEqualTo(SopmodamMemberType.NOT_TARGET);
		}
	}

	@Nested
	@DisplayName("기준 기수는 회차의 기수다")
	class RoundGenerationBase {

		@Test
		@DisplayName("같은 사용자라도 회차 기수에 따라 유형이 달라진다")
		void 회차_기수에_따라_유형이_달라진다() {
			InternalUserDetails user = user(36, sopt(34), sopt(36));

			assertThat(memberPolicy.resolveType(user, 35)).isEqualTo(SopmodamMemberType.NOT_TARGET);
			assertThat(memberPolicy.resolveType(user, 36)).isEqualTo(SopmodamMemberType.ACTIVE_OB);
			assertThat(memberPolicy.resolveType(user, 37)).isEqualTo(SopmodamMemberType.HONORARY);
		}
	}

	@Nested
	@DisplayName("권한 매트릭스 (명세 §2.5)")
	class Permission {

		@ParameterizedTest(name = "{0}: 투표 {1}, 답변 {2}")
		@DisplayName("유형별 투표·답변 가능 여부")
		@CsvSource({
			"ACTIVE_YB,  true,  false",
			"ACTIVE_OB,  true,  true",
			"HONORARY,   false, true",
			"NOT_TARGET, false, false",
		})
		void 유형별_권한(SopmodamMemberType memberType, boolean canVote, boolean canAnswer) {
			assertThat(memberType.isCanVote()).isEqualTo(canVote);
			assertThat(memberType.isCanAnswer()).isEqualTo(canAnswer);
		}

		@ParameterizedTest
		@DisplayName("활동 YB·OB 는 투표 권한 검증을 통과한다")
		@EnumSource(value = SopmodamMemberType.class, names = {"ACTIVE_YB", "ACTIVE_OB"})
		void 활동_기수는_투표_가능(SopmodamMemberType memberType) {
			assertThatCode(() -> memberPolicy.validateCanVote(memberType)).doesNotThrowAnyException();
		}

		@ParameterizedTest
		@DisplayName("명예기수·대상 아님은 투표 권한 검증에서 403")
		@EnumSource(value = SopmodamMemberType.class, names = {"HONORARY", "NOT_TARGET"})
		void 활동_기수가_아니면_투표_불가(SopmodamMemberType memberType) {
			assertThatThrownBy(() -> memberPolicy.validateCanVote(memberType))
				.isInstanceOf(ForbiddenException.class)
				.hasMessageContaining("질문 투표는 활동 기수만 참여할 수 있습니다.");
		}
	}

	private static InternalUserDetails user(int lastGeneration, SoptActivity... activities) {
		return new InternalUserDetails(1L, "홍길동", null, null, null, null, lastGeneration, List.of(activities));
	}

	private static SoptActivity sopt(int generation) {
		return new SoptActivity(generation, generation, "서버", null, "MEMBER", true);
	}

	private static SoptActivity makers(int generation) {
		return new SoptActivity(1000 + generation, generation, "서버", "메이커스", "MEMBER", false);
	}
}
