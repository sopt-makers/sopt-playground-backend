package org.sopt.makers.internal.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateDefaultUserProfileRequest(
        @Schema(description = "생성할 유저 ID", example = "100", required = true)
        Long userId
) {
}
