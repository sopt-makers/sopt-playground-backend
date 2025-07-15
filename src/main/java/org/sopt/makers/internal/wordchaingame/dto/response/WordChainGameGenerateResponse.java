package org.sopt.makers.internal.wordchaingame.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record WordChainGameGenerateResponse(
        @Schema(required = true)
        Long roomId,
        String word,
        WordChainGameGenerateUserResponse user
) { }
