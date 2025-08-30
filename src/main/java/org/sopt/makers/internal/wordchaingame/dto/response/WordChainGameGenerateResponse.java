package org.sopt.makers.internal.wordchaingame.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.makers.internal.external.platform.MemberSimpleResonse;

public record WordChainGameGenerateResponse(
        @Schema(required = true)
        Long roomId,
        String word,
        MemberSimpleResonse user
) { }
