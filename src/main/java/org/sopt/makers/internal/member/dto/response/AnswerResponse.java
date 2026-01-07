package org.sopt.makers.internal.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AnswerResponse(
	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "답변 ID")
	Long answerId,

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "답변 내용")
	String content,

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "도움돼요 반응 수")
	Long reactionCount,

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "현재 사용자가 도움돼요를 눌렀는지 여부")
	Boolean isReacted,

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "생성일시")
	String createdAt
) {
}
