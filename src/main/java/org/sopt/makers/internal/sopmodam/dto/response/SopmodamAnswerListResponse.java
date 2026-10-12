package org.sopt.makers.internal.sopmodam.dto.response;

import java.util.List;

public record SopmodamAnswerListResponse(
    Long roundId,
    String eventName,
    SopmodamQuestionResponse question,
    int answerCount,
    List<SopmodamAnswerItemResponse> answers
) {}
