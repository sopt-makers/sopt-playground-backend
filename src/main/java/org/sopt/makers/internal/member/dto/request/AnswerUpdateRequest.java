package org.sopt.makers.internal.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AnswerUpdateRequest(

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "수정할 답변 내용 (최대 2,000자)")
	@NotBlank(message = "답변 내용은 공백일 수 없습니다.")
	@Size(max = 2000, message = "답변은 최대 2,000자까지 입력 가능합니다.")
	String content
) {
}
