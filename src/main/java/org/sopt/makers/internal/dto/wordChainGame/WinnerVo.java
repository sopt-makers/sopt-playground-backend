package org.sopt.makers.internal.dto.wordChainGame;

public record WinnerVo(
        Long roomId,
        UserResponse winner
) {
    public record UserResponse(
            Long id,
            String name,
            String profileImage
    ) {}
}
