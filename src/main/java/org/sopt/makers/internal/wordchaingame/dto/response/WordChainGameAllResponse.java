package org.sopt.makers.internal.wordchaingame.dto.response;

import java.util.List;

public record WordChainGameAllResponse(
        List<WordChainGameRoomResponse> rooms,
        Boolean hasNext
) {}
