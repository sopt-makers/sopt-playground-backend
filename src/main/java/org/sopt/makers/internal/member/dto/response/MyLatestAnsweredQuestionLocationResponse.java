package org.sopt.makers.internal.member.dto.response;

public record MyLatestAnsweredQuestionLocationResponse(
        Long questionId,
        Integer page,
        Integer index
) {}
