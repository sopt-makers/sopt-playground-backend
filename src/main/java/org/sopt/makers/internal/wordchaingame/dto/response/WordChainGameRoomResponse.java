package org.sopt.makers.internal.wordchaingame.dto.response;

import org.sopt.makers.internal.external.platform.MemberSimpleResonse;

import java.util.List;

public record WordChainGameRoomResponse(
        Long roomId,
        String startWord,
        MemberSimpleResonse startUser,
        List<WordResponse> words
) {
    public record WordResponse(
            String word,
            MemberSimpleResonse user
    ) { }
}
