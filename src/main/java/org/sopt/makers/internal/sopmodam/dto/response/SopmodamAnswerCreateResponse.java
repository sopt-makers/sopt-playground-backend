package org.sopt.makers.internal.sopmodam.dto.response;

import java.time.LocalDateTime;

import org.sopt.makers.internal.sopmodam.domain.SopmodamAnswer;

public record SopmodamAnswerCreateResponse(
    Long answerId,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static SopmodamAnswerCreateResponse from(SopmodamAnswer answer) {
        return new SopmodamAnswerCreateResponse(
            answer.getId(),
            answer.getContent(),
            answer.getCreatedAt(),
            answer.getUpdatedAt()
        );
    }
}
