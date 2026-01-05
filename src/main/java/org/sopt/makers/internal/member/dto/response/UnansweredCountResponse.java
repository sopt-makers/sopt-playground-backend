package org.sopt.makers.internal.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UnansweredCountResponse(
	@Schema(required = true, description = "답변 대기 중인 질문 개수")
	Long count
) {
}
