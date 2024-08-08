package org.sopt.makers.internal.dto.member;

import java.util.List;

public record CoffeeChatResponse(
	List<CoffeeChatVo> coffeeChatList,
	Boolean hasNext,
	Integer totalCount
) {
	public record CoffeeChatVo(
		Long memberId,
		String name,
		String memberProfileImage,
		String organization,
		String careerTitle,
		String coffeeChatBio
	) { }
}
