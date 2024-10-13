package org.sopt.makers.internal.member.dto.request;

import org.sopt.makers.internal.member.domain.coffeechat.Career;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatSection;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatTopicType;
import org.sopt.makers.internal.member.domain.coffeechat.MeetingType;

import java.util.List;

public record CoffeeChatDetailsRequest(
		MemberInfoRequest memberInfo,
		CoffeeChatInfo coffeeChatInfo
) {

	public record MemberInfoRequest(
			Career career,
			String introduction
	) { }

	public record CoffeeChatInfo(
			List<CoffeeChatSection> sections,
			String bio,
			List<CoffeeChatTopicType> topicTypes,
			String topic,
			MeetingType meetingType,
			String guideline
	) { }
}
