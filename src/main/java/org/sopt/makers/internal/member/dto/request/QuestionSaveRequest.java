package org.sopt.makers.internal.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record QuestionSaveRequest(

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "질문 내용 (최대 2,000자)")
	@NotBlank(message = "질문 내용은 공백일 수 없습니다.")
	@Size(max = 2000, message = "질문은 최대 2,000자까지 입력 가능합니다.")
	String content,

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "익명 여부")
	@NotNull(message = "익명 여부는 필수입니다.")
	Boolean isAnonymous,

	@Schema(
		description = "익명인 경우 최신 기수 정보 (예: '36기 서버', '35기 기획')",
		example = "36기 서버",
		requiredMode = Schema.RequiredMode.NOT_REQUIRED
	)
	String latestSoptActivity
) {
}
