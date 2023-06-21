package org.sopt.makers.internal.dto.wordChainGame;

import java.util.List;

public record WordChainGameRoomLastwordResponse(
        Long roomId,
        String word,
        UserResponse user
) {

    public record UserResponse(
            Long id,
            String profileImage,
            String name
    ) {}
}
