package org.sopt.makers.internal.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record QuestionsResponse(
	@Schema(required = true, description = "질문 목록")
	List<QuestionResponse> questions,

	@Schema(required = true, description = "다음 페이지가 있는지 여부")
	Boolean hasNext,

	@Schema(description = "다음 페이지 cursor (마지막 질문 ID)")
	Long nextCursor
) {
}
