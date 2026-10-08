package org.sopt.makers.internal.sopmodam.dto.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SopmodamRoundUpdateRequest(
    @Schema(required = true, description = "회차 기수. 사용자 유형(활동 YB·OB, 명예기수)을 나누는 기준", example = "39")
    @NotNull(message = "필요한 값이 없습니다")
    @Positive(message = "기수는 양수여야 합니다.")
    Integer generation,

    @Schema(required = true, description = "회차 주제", example = "SOPT 첫 협업")
    @NotBlank(message = "필요한 값이 없습니다")
    @Size(max = 255, message = "회차 주제는 255자 이하로 입력해야 합니다.")
    String eventName,

    @Schema(required = true, description = "투표 시작 시각 (KST). 투표가 시작된 뒤에는 바꿀 수 없음", example = "2026-10-10T00:00:00")
    @NotNull(message = "필요한 값이 없습니다")
    LocalDateTime voteStartAt,

    @Schema(required = true, description = "투표 마감 시각 (KST). 투표가 끝난 뒤에는 바꿀 수 없음", example = "2026-10-15T00:00:00")
    @NotNull(message = "필요한 값이 없습니다")
    LocalDateTime voteEndAt,

    @Schema(required = true, description = "답변 모집 마감 시각이자 공개 시각 (KST)", example = "2026-11-05T00:00:00")
    @NotNull(message = "필요한 값이 없습니다")
    LocalDateTime answerEndAt
) {
}
