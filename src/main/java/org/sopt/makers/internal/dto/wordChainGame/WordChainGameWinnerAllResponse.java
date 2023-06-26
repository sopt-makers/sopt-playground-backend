package org.sopt.makers.internal.dto.wordChainGame;

import java.util.List;

public record WordChainGameWinnerAllResponse(
        List<WinnerVo> winners,
        Boolean hasNext
) {}
