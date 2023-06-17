package org.sopt.makers.internal.dto.wordChainGame;

import java.util.List;

public record WordChainGameAllResponse(
        List<WordChainGameRoomResponse> rooms,
        Boolean hasNext
) {}
