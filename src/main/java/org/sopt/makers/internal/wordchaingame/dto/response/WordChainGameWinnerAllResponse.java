package org.sopt.makers.internal.wordchaingame.dto.response;

import java.util.List;

public record WordChainGameWinnerAllResponse(
        List<WordChainGameWinnerResponse> winners,
        Boolean hasNext
) {}
