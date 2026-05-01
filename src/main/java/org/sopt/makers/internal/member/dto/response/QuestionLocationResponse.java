package org.sopt.makers.internal.member.dto.response;

import org.sopt.makers.internal.member.domain.QuestionTab;

public record QuestionLocationResponse(
	Long questionId,
	QuestionTab tab,
	Integer page,
	Integer index
) {}
