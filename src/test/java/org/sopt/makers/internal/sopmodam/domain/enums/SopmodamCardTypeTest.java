package org.sopt.makers.internal.sopmodam.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * 홈 카드 노출 매트릭스 테스트.
 *
 * <p>진행 단계 × 사용자 유형의 모든 조합을 확인한다. 기대값이 비어 있으면 카드를 보여주지 않는다(204).
 * <pre>
 *   phase / 유형   ACTIVE_YB          ACTIVE_OB           HONORARY            NOT_TARGET
 *   SCHEDULED      -                  -                   -                   -
 *   VOTING         VOTE               VOTE                -                   -
 *   ANSWERING      ANSWER_PREPARING   ANSWER_COLLECTING   ANSWER_COLLECTING   -
 *   REVEALED       REVEALED           REVEALED            REVEALED            -
 * </pre>
 */
class SopmodamCardTypeTest {

	@ParameterizedTest(name = "{0} · {1} → {2}")
	@DisplayName("진행 단계와 사용자 유형으로 카드 유형이 정해진다")
	@CsvSource({
		"SCHEDULED, ACTIVE_YB,  ",
		"SCHEDULED, ACTIVE_OB,  ",
		"SCHEDULED, HONORARY,   ",
		"SCHEDULED, NOT_TARGET, ",
		"VOTING,    ACTIVE_YB,  VOTE",
		"VOTING,    ACTIVE_OB,  VOTE",
		"VOTING,    HONORARY,   ",
		"VOTING,    NOT_TARGET, ",
		"ANSWERING, ACTIVE_YB,  ANSWER_PREPARING",
		"ANSWERING, ACTIVE_OB,  ANSWER_COLLECTING",
		"ANSWERING, HONORARY,   ANSWER_COLLECTING",
		"ANSWERING, NOT_TARGET, ",
		"REVEALED,  ACTIVE_YB,  REVEALED",
		"REVEALED,  ACTIVE_OB,  REVEALED",
		"REVEALED,  HONORARY,   REVEALED",
		"REVEALED,  NOT_TARGET, ",
	})
	void 노출_매트릭스(SopmodamPhase phase, SopmodamMemberType memberType, SopmodamCardType expected) {
		assertThat(SopmodamCardType.of(phase, memberType)).isEqualTo(Optional.ofNullable(expected));
	}
}
