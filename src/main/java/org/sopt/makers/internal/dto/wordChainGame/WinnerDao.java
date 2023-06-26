package org.sopt.makers.internal.dto.wordChainGame;

import com.querydsl.core.annotations.QueryProjection;

public record WinnerDao(
        Long id,
        Long roomId,
        Long memberId,
        String name,
        String profileImage
){
    @QueryProjection
    public WinnerDao {}
}
