package org.sopt.makers.internal.member.dto.profile;

/**
 * 기본 인적사항. 가중치에서는 항목당 1점으로 동일하게 취급된다.
 * QueryDSL 이 생성자를 찾으려면 public 이어야 한다.
 */
public record MemberBasicInfoVo(
	String address,
	String university,
	String major
) {

	public int filledCount() {
		return ProfileFieldSupport.countFilled(address, university, major);
	}

	/** 검색어가 대학교명에 포함되는지. 기존 로직과 동일하게 공백 여부는 보지 않는다. */
	public boolean matchesUniversity(String keyword) {
		return university != null && university.contains(keyword);
	}
}
