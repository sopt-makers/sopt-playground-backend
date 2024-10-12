package org.sopt.makers.internal.dto.member;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record CoffeeChatRequest(
		@NotNull(message = "수신자 정보는 필수 입력 값입니다.")
		Long receiverId,

        @JsonProperty(required = false) String senderEmail,
        @JsonProperty(required = false) String senderPhone,

		@NotBlank(message = "수신 카테고리는 필수 입력 값입니다.")
		String category,

		@NotBlank(message = "수신 본문은 필수 입력 값입니다.")
		@Size(max = 40, message = "본문은 ??자를 초과할 수 없습니다.")  // TODO 정책 fix 되면 반영 예정
		String content
) {}
