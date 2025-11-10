package org.sopt.makers.internal.coffeechat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.makers.internal.coffeechat.domain.enums.ChatCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static org.sopt.makers.internal.common.Constant.PHONE_NUMBER_REGEX;

public record CoffeeChatRequest(
		@Schema(required = true, description = "수신자 ID")
		@NotNull(message = "수신자 정보는 필수 입력 값입니다.")
		Long receiverId,

		@Schema(required = true, description = "발신자 전화번호 (하이픈 제외)")
		@NotBlank(message = "발신자 전화번호는 필수 입력 값입니다.")
		@Pattern(regexp = PHONE_NUMBER_REGEX, message = "잘못된 전화번호 형식입니다. '-'을 제외한 11자의 번호를 입력해주세요.")
		String senderPhone,

		@Schema(required = true, description = "카테고리 (COFFEE_CHAT 또는 NOTE)")
		@NotNull(message = "카테고리는 필수 입력 값입니다.")
		ChatCategory category,

		@Schema(required = true, description = "문의 내용")
		@NotBlank(message = "수신 본문은 필수 입력 값입니다.")
		@Size(max = 500, message = "본문은 500자를 초과할 수 없습니다.")
		String content
) {
}
