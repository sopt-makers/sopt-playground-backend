package org.sopt.makers.internal.sopmodam.dto.response;

import java.time.LocalDateTime;

import org.sopt.makers.internal.sopmodam.domain.SopmodamAnswer;

public record SopmodamAnswerUpdateResponse(
    Long answerId,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static SopmodamAnswerUpdateResponse from(SopmodamAnswer answer) {
        return new SopmodamAnswerUpdateResponse(
            answer.getId(),
            answer.getContent(),
            answer.getCreatedAt(),
            answer.getUpdatedAt()
        );
    }
}
