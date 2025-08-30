package org.sopt.makers.internal.wordchaingame.dto.response;

import org.sopt.makers.internal.external.platform.MemberSimpleResonse;

public record WordChainGameWinnerResponse(
        Long roomId,
        MemberSimpleResonse winner
) { }