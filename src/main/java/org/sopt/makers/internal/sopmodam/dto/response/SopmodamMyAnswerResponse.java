package org.sopt.makers.internal.sopmodam.dto.response;

import java.time.LocalDateTime;

public record SopmodamMyAnswerResponse(
    Long roundId,
    SopmodamQuestionResponse question,
    LocalDateTime answerEndAt,
    boolean canEdit,
    // 아직 작성하지 않았으면 null
    SopmodamAnswerResponse answer
) {
}
