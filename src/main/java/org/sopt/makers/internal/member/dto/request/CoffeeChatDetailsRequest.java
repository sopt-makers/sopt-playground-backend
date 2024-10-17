package org.sopt.makers.internal.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.makers.internal.member.domain.coffeechat.Career;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatSection;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatTopicType;
import org.sopt.makers.internal.member.domain.coffeechat.MeetingType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

public record CoffeeChatDetailsRequest(
		MemberInfoRequest memberInfo,
		CoffeeChatInfo coffeeChatInfo
) {

	public record MemberInfoRequest(
			@Schema(required = true)
			Career career,

			@Schema(required = true)
			@NotBlank(message = "자기소개는 필수 입력 값입니다.")
			@Size(max = 200, message = "자기소개는 200자를 초과할 수 없습니다.")
			String introduction
	) { }

	public record CoffeeChatInfo(
			@Schema(required = true)
			List<CoffeeChatSection> sections,

			@Schema(required = true)
			@NotBlank(message = "커피챗 제목은 필수 입력 값입니다.")
			@Size(max = 40, message = "커피챗 제목은 40자를 초과할 수 없습니다.")
			String bio,

			@Schema(required = true)
			List<CoffeeChatTopicType> topicTypes,

			@Schema(required = true)
			@NotBlank(message = "커피챗 주제는 필수 입력 값입니다.")
			@Size(max = 200, message = "커피챗 주제는 200자를 초과할 수 없습니다.")
			String topic,

			@Schema(required = true)
			MeetingType meetingType,

			@Size(max = 1000, message = "유의사항은 1000자를 초과할 수 없습니다.")
			String guideline
	) { }
}
