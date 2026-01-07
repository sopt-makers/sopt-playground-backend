package org.sopt.makers.internal.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.makers.internal.community.dto.AnonymousProfileVo;

public record QuestionResponse(
	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "질문 ID")
	Long questionId,

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "질문 내용")
	String content,

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "질문자 ID")
	Long askerId,

	@Schema(description = "질문자 이름 (익명일 경우 null)")
	String askerName,

	@Schema(description = "질문자 프로필 이미지 (익명일 경우 null)")
	String askerProfileImage,

	@Schema(description = "질문자 최신 기수 정보 (예: '36기 서버')")
	String askerLatestGeneration,

	@Schema(description = "익명 프로필 정보 (익명일 경우)")
	AnonymousProfileVo anonymousProfile,

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "익명 여부")
	Boolean isAnonymous,

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "나도 궁금해요 반응 수")
	Long reactionCount,

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "현재 사용자가 나도 궁금해요를 눌렀는지 여부")
	Boolean isReacted,

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "답변 여부")
	Boolean isAnswered,

	@Schema(description = "답변 정보 (답변이 있을 경우)")
	AnswerResponse answer,

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "생성일시")
	String createdAt,

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "질문이 일주일 이내에 작성되었는지 여부")
	Boolean isNew,

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "본인의 질문인지 여부")
	Boolean isMine,

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "본인이 받은 질문인지 여부")
	Boolean isReceived
) {
}
