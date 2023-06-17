package org.sopt.makers.internal.mapper;

import org.mapstruct.Mapper;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.WordChainGameRoom;
import org.sopt.makers.internal.dto.wordChainGame.WordChainGameGenerateRequest;
import org.sopt.makers.internal.dto.wordChainGame.WordChainGameGenerateResponse;
import org.sopt.makers.internal.dto.wordChainGame.WordChainGameRoomResponse;

@Mapper(componentModel = "spring")
public interface WordChainGameMappper {
    WordChainGameGenerateResponse toGenerateResponse(Member member, WordChainGameGenerateRequest request);
    WordChainGameGenerateResponse toGenerateResponse(Member member, WordChainGameRoom wordChainGameRoom);
    WordChainGameGenerateResponse toGenerateResponse(WordChainGameGenerateResponse.UserResponse member, WordChainGameRoom wordChainGameRoom);
    WordChainGameRoomResponse toGetAllResponse(WordChainGameRoom wordChainGameRoom);
}
