package org.sopt.makers.internal.member.dto.profile;

/**
 * 성향 영역. 가중치에서는 항목당 1점으로 동일하게 취급된다.
 */
public record MemberPersonalityVo(
	String mbti,
	String mbtiDescription,
	Double sojuCapacity,
	String interest,
	String idealType
) {

	/** sojuCapacity 는 숫자라 null 여부만, 나머지는 공백 여부까지 본다 (기존 로직과 동일). */
	public int filledCount() {
		return ProfileFieldSupport.countFilled(mbti, mbtiDescription, interest, idealType)
			+ ProfileFieldSupport.countNonNull(sojuCapacity);
	}
}
