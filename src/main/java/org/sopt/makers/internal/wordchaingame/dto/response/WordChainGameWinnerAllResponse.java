package org.sopt.makers.internal.wordchaingame.dto.response;

import java.util.List;

public record WordChainGameWinnerAllResponse(
        List<WinnerVo> winners,
        Boolean hasNext
) {}
