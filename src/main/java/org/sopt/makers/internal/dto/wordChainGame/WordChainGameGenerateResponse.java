package org.sopt.makers.internal.dto.wordChainGame;

import io.swagger.v3.oas.annotations.media.Schema;

public record WordChainGameGenerateResponse(
        @Schema(required = true)
        Long roomId,
        String word,
        UserResponse user
) {
    public record UserResponse(
            Long id,
            String profileImage,
            String name
    ){}
}
