package org.sopt.makers.internal.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AnswerResponse(
	@Schema(required = true, description = "답변 ID")
	Long answerId,

	@Schema(required = true, description = "답변 내용")
	String content,

	@Schema(required = true, description = "도움돼요 반응 수")
	Long reactionCount,

	@Schema(required = true, description = "현재 사용자가 도움돼요를 눌렀는지 여부")
	Boolean isReacted,

	@Schema(required = true, description = "생성일시 (ISO 8601 형식)")
	String createdAt
) {
}
