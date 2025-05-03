package org.sopt.makers.internal.wordchaingame.dto.response;

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
