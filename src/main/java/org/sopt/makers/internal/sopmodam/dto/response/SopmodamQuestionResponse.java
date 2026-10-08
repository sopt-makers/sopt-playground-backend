package org.sopt.makers.internal.sopmodam.dto.response;

import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;

public record SopmodamQuestionResponse(
    Long questionId,
    String content
) {
    public static SopmodamQuestionResponse from(SopmodamQuestion question) {
        return new SopmodamQuestionResponse(question.getId(), question.getContent());
    }
}
