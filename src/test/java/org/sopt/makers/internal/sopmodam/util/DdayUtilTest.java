package org.sopt.makers.internal.sopmodam.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * dDay 계산 테스트.
 *
 * <p>규칙: {@code (마감 시각 - 1초)의 날짜 - 오늘 날짜}. 마감 시각이 지났으면 {@code null}.
 * 운영자는 마감 시각을 다음 날 00:00:00 으로 넣으므로, 마감 전날 하루 종일이 D-Day(0) 여야 한다.
 * 1초를 빼지 않으면 마감 전날이 D-1 로 보이는 off-by-one 이 생긴다.
 */
class DdayUtilTest {

	@Nested
	@DisplayName("마감 시각이 자정일 때 (운영 권장값)")
	class MidnightDeadline {

		private final LocalDateTime deadline = LocalDateTime.of(2026, 10, 15, 0, 0);

		@Test
		@DisplayName("명세 예시: 10월 10일에는 D-4")
		void 명세_예시_D4() {
			assertThat(DdayUtil.calculateDDay(deadline, LocalDateTime.of(2026, 10, 10, 9, 0))).isEqualTo(4);
		}

		@Test
		@DisplayName("마감 전날 0시 정각은 D-Day")
		void 마감_전날_0시는_D_Day() {
			assertThat(DdayUtil.calculateDDay(deadline, LocalDateTime.of(2026, 10, 14, 0, 0))).isZero();
		}

		@Test
		@DisplayName("마감 1초 전도 D-Day")
		void 마감_1초_전은_D_Day() {
			assertThat(DdayUtil.calculateDDay(deadline, LocalDateTime.of(2026, 10, 14, 23, 59, 59))).isZero();
		}

		@Test
		@DisplayName("마감 직전 나노초 단위 시각도 D-Day")
		void 마감_직전_나노초는_D_Day() {
			LocalDateTime now = LocalDateTime.of(2026, 10, 14, 23, 59, 59, 999_999_999);
			assertThat(DdayUtil.calculateDDay(deadline, now)).isZero();
		}

		@Test
		@DisplayName("마감 이틀 전 마지막 순간은 D-1")
		void 마감_이틀_전_마지막_순간은_D1() {
			assertThat(DdayUtil.calculateDDay(deadline, LocalDateTime.of(2026, 10, 13, 23, 59, 59))).isEqualTo(1);
		}

		@Test
		@DisplayName("마감 정각이면 null (종료 시각은 포함하지 않는다)")
		void 마감_정각은_null() {
			assertThat(DdayUtil.calculateDDay(deadline, deadline)).isNull();
		}

		@Test
		@DisplayName("마감이 지났으면 null")
		void 마감_이후는_null() {
			assertThat(DdayUtil.calculateDDay(deadline, LocalDateTime.of(2026, 10, 20, 12, 0))).isNull();
		}
	}

	@Nested
	@DisplayName("마감 시각이 자정이 아닐 때")
	class NonMidnightDeadline {

		private final LocalDateTime deadline = LocalDateTime.of(2026, 10, 15, 12, 0);

		@Test
		@DisplayName("마감 당일 마감 전이면 D-Day")
		void 마감_당일_마감_전은_D_Day() {
			assertThat(DdayUtil.calculateDDay(deadline, LocalDateTime.of(2026, 10, 15, 11, 59, 59))).isZero();
		}

		@Test
		@DisplayName("마감 전날 마감 시각 이후여도 D-1")
		void 마감_전날은_D1() {
			assertThat(DdayUtil.calculateDDay(deadline, LocalDateTime.of(2026, 10, 14, 13, 0))).isEqualTo(1);
		}

		@Test
		@DisplayName("마감 정각이면 null")
		void 마감_정각은_null() {
			assertThat(DdayUtil.calculateDDay(deadline, deadline)).isNull();
		}
	}

	@Nested
	@DisplayName("월·연도 경계")
	class CalendarBoundary {

		@Test
		@DisplayName("마감이 다음 달 1일 0시면 이번 달 말일이 D-Day")
		void 월말_경계() {
			LocalDateTime deadline = LocalDateTime.of(2026, 11, 1, 0, 0);
			assertThat(DdayUtil.calculateDDay(deadline, LocalDateTime.of(2026, 10, 31, 8, 0))).isZero();
			assertThat(DdayUtil.calculateDDay(deadline, LocalDateTime.of(2026, 10, 29, 8, 0))).isEqualTo(2);
		}

		@Test
		@DisplayName("마감이 1월 1일 0시면 12월 31일이 D-Day")
		void 연말_경계() {
			LocalDateTime deadline = LocalDateTime.of(2027, 1, 1, 0, 0);
			assertThat(DdayUtil.calculateDDay(deadline, LocalDateTime.of(2026, 12, 31, 0, 0))).isZero();
			assertThat(DdayUtil.calculateDDay(deadline, LocalDateTime.of(2026, 12, 28, 12, 0))).isEqualTo(3);
		}
	}
}
