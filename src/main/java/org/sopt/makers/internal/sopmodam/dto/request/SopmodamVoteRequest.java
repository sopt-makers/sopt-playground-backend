package org.sopt.makers.internal.sopmodam.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

public record SopmodamVoteRequest(
    @Schema(required = true, description = "투표할 질문 ID. 해당 회차의 질문이어야 함")
    @NotNull(message = "필요한 값이 없습니다")
    Long questionId
) {
}
