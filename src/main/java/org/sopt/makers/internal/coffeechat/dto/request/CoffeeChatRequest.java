package org.sopt.makers.internal.coffeechat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.coffeechat.domain.enums.ChatCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
		@Size(max = 500, message = "본문은 500자를 초과할 수 없습니다.")
		String content
) {
	public CoffeeChatRequest {
		if (senderEmail == null && senderPhone == null) {
			throw new ClientBadRequestException("발신자 이메일 또는 전화번호 중 하나는 필수 입력 값입니다.");
		}
		if (senderEmail != null && senderPhone != null) {
			throw new ClientBadRequestException("발신자 이메일과 전화번호는 모두 요청이 불가능합니다.");
		}
	}
}
