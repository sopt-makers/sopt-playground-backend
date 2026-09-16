package org.sopt.makers.internal.member.dto.profile;

/**
 * 정렬용 VO 들이 공유하는 "값이 채워졌는가" 판정.
 * 기존 가중치 전략이 쓰던 (null 이 아니고 공백도 아님) 검사와 동일한 의미를 유지한다.
 */
final class ProfileFieldSupport {

	private ProfileFieldSupport() {
	}

	static boolean isFilled(String value) {
		return value != null && !value.isBlank();
	}

	static int countFilled(String... values) {
		int count = 0;
		for (String value : values) {
			if (isFilled(value)) {
				count++;
			}
		}
		return count;
	}

	static int countNonNull(Object... values) {
		int count = 0;
		for (Object value : values) {
			if (value != null) {
				count++;
			}
		}
		return count;
	}
}
