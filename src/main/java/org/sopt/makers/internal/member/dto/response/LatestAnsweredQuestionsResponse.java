package org.sopt.makers.internal.member.dto.response;

import java.util.List;

public record LatestAnsweredQuestionsResponse(
	List<LatestQuestionCardResponse> questions
) {
	public record LatestQuestionCardResponse(
		Long receiverId,
		String receiverName,
		String receiverProfileImage,
		Long questionId,
		String content,
		QuestionLocationResponse location
	) {}
}
