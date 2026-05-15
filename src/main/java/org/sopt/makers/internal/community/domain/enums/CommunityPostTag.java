package org.sopt.makers.internal.community.domain.enums;

// 화면 표시용 카테고리 태그
public enum CommunityPostTag {
	FREE("자유"),
	MEETING("모임"),
	EVENT("행사"),
	PROJECT("프로젝트"),
	RECRUIT("채용"),
	PROMOTION("홍보"),
	SOPTICLE("솝티클");

	private final String label;

	CommunityPostTag(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}