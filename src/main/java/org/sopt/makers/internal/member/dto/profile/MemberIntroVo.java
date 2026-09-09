package org.sopt.makers.internal.member.dto.profile;

/**
 * 자기표현 영역.
 * introduction 만 가중치가 다르므로(3점) 개수 대신 항목별 판정을 노출한다.
 */
public record MemberIntroVo(
	String introduction,
	String selfIntroduction,
	String skill
) {

	public boolean hasIntroduction() {
		return ProfileFieldSupport.isFilled(introduction);
	}

	public boolean hasSelfIntroduction() {
		return ProfileFieldSupport.isFilled(selfIntroduction);
	}

	public boolean hasSkill() {
		return ProfileFieldSupport.isFilled(skill);
	}
}
