package org.sopt.makers.internal.resolution.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LuckyPickResponse(
        @Schema(description = "당첨 여부", example = "true")
        Boolean isWinner
) {
}
