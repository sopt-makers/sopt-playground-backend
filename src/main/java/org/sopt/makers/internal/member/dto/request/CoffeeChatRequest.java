package org.sopt.makers.internal.member.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.makers.internal.member.domain.coffeechat.ChatCategory;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import static org.sopt.makers.internal.common.Constant.PHONE_NUMBER_REGEX;

public record CoffeeChatRequest(
		@Schema(required = true)
		@NotNull(message = "수신자 정보는 필수 입력 값입니다.")
		Long receiverId,

        @JsonProperty(required = false) String senderEmail,
        @JsonProperty(required = false)
		@Pattern(regexp = PHONE_NUMBER_REGEX, message = "잘못된 전화번호 형식입니다. '-'을 제외한 11자의 번호를 입력해주세요.")
		String senderPhone,

		@Schema(required = true)
		ChatCategory category,

		@Schema(required = true)
		@NotBlank(message = "수신 본문은 필수 입력 값입니다.")
		@Size(max = 40, message = "본문은 40자를 초과할 수 없습니다.")  // TODO 정책 fix 되면 반영 예정
		String content
) {}
