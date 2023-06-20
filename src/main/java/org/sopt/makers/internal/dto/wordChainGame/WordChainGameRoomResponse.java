package org.sopt.makers.internal.dto.wordChainGame;

import java.util.List;

public record WordChainGameRoomResponse(
        Long roomId,
        String startWord,
        WordResponse.UserResponse startUser,
        List<WordResponse> words
) {

    public record WordResponse(
            String word,
            UserResponse user
    ) {
        public record UserResponse(
                Long id,
                String profileImage,
                String name
        ) {}
    }
}
