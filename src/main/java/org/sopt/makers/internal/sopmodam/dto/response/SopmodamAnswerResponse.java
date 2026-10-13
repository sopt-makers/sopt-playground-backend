package org.sopt.makers.internal.sopmodam.dto.response;

import java.time.LocalDateTime;

import org.sopt.makers.internal.sopmodam.domain.SopmodamAnswer;

public record SopmodamAnswerResponse(
    Long answerId,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static SopmodamAnswerResponse from(SopmodamAnswer answer) {
        return new SopmodamAnswerResponse(answer.getId(), answer.getContent(), answer.getCreatedAt(), answer.getUpdatedAt());
    }
}
