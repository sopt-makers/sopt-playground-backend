package org.sopt.makers.internal.sopmodam.dto.response;

import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;

public record SopmodamQuestionResultResponse(
    Long questionId,
    String content,
    int voteCount,
    boolean isSelected
) {
    public static SopmodamQuestionResultResponse from(SopmodamQuestion question) {
        return new SopmodamQuestionResultResponse(
            question.getId(),
            question.getContent(),
            question.getVoteCount(),
            question.isSelected()
        );
    }
}
