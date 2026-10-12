package org.sopt.makers.internal.sopmodam.dto.response;

import java.time.LocalDateTime;

import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.sopmodam.domain.SopmodamAnswer;

public record SopmodamAnswerItemResponse(
    Long answerId,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    SopmodamWriterResponse writer
) {
    public static SopmodamAnswerItemResponse of(SopmodamAnswer answer, InternalUserDetails user) {
        return new SopmodamAnswerItemResponse(
            answer.getId(),
            answer.getContent(),
            answer.getCreatedAt(),
            answer.getUpdatedAt(),
            SopmodamWriterResponse.from(user)
        );
    }
}
