package org.sopt.makers.internal.dto.member;

import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatSection;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatTopicType;

import java.util.List;

public record CoffeeChatResponse(
	List<CoffeeChatVo> coffeeChatList,
	Integer totalCount
) {
	public record CoffeeChatVo(
		Long memberId,
		String name,
		String memberProfileImage,
		String organization,
		String careerTitle,
		String coffeeChatBio,
		List<CoffeeChatTopicType> topicTypes,
		List<CoffeeChatSection> sections
	) { }
}
