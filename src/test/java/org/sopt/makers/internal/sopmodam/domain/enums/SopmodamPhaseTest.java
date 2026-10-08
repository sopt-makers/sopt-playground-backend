package org.sopt.makers.internal.sopmodam.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;

/**
 * 진행 단계 판정 테스트.
 *
 * <p>일정 경계는 <b>시작 포함, 종료 미포함</b>이다. 각 경계 시각의 직전·정각을 모두 확인한다.
 * <pre>
 *   voteStartAt  2026-10-10T00:00   이 시각부터 VOTING
 *   voteEndAt    2026-10-15T00:00   이 시각부터 ANSWERING
 *   answerEndAt  2026-11-05T00:00   이 시각부터 REVEALED
 * </pre>
 */
class SopmodamPhaseTest {

	private final SopmodamRound round = SopmodamRound.builder()
		.generation(37)
		.eventName("SOPT 첫 협업")
		.voteStartAt(LocalDateTime.of(2026, 10, 10, 0, 0))
		.voteEndAt(LocalDateTime.of(2026, 10, 15, 0, 0))
		.answerEndAt(LocalDateTime.of(2026, 11, 5, 0, 0))
		.build();

	@ParameterizedTest(name = "{0} → {1}")
	@DisplayName("경계 시각의 직전과 정각에서 단계가 바뀐다")
	@CsvSource({
		"2026-10-01T00:00:00,           SCHEDULED",
		"2026-10-09T23:59:59.999999999, SCHEDULED",
		"2026-10-10T00:00:00,           VOTING",
		"2026-10-14T23:59:59.999999999, VOTING",
		"2026-10-15T00:00:00,           ANSWERING",
		"2026-11-04T23:59:59.999999999, ANSWERING",
		"2026-11-05T00:00:00,           REVEALED",
		"2027-03-01T00:00:00,           REVEALED",
	})
	void 경계_시각_판정(LocalDateTime now, SopmodamPhase expected) {
		assertThat(SopmodamPhase.of(round, now)).isEqualTo(expected);
	}
}
