package org.sopt.makers.internal.wordchaingame.dto.response;

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
