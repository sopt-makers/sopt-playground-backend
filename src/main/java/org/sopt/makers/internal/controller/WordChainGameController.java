package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.common.InfiniteScrollUtil;
import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.dto.wordChainGame.*;
import org.sopt.makers.internal.exception.WordChainGameHasWrongInputException;
import org.sopt.makers.internal.mapper.MemberMapper;
import org.sopt.makers.internal.service.MemberService;
import org.sopt.makers.internal.service.WordChainGameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chainWordGame")
@SecurityRequirement(name = "Authorization")
@Tag(name = "끝말잇기 게임", description = "끝말잇기 게임과 관련 API들")
public class WordChainGameController {
    private final MemberService memberService;
    private final WordChainGameService wordChainGameService;
    private final MemberMapper memberMapper;
    private final InfiniteScrollUtil infiniteScrollUtil;

    @Operation(summary = "단어보내기 API")
    @PostMapping("/wordGame")
    public ResponseEntity<WordChainGameGenerateResponse> getPostMapping(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @RequestBody WordChainGameGenerateRequest request
    ) {
        val member = memberService.getMemberById(memberDetails.getId());
        val responseMember = memberMapper.toUserResponse(member);
        if(request.word().length() < 2) throw new WordChainGameHasWrongInputException("한글자 단어는 사용할 수 없어요.");
        wordChainGameService.createWord(member, request);
        val response = new WordChainGameGenerateResponse(request.roomId(), request.word(), responseMember);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "게임 전체 조회", description = "cursor : 처음에는 null 또는 0, 이후 방번호 증 마지막 room의 id")
    @GetMapping("/gameRoom")
    public ResponseEntity<WordChainGameAllResponse> getPostMapping(
            @RequestParam(required = false, name = "limit") Integer limit,
            @RequestParam(required = false, name = "cursor") Long cursor
    ) {
        val rooms = wordChainGameService.getAllRoom(checkLimitForPagination(limit), cursor);
        val roomList = rooms.stream().map(room -> {
            val isFirstGame = Objects.isNull(room.getCreatedUserId());
            val startUser = isFirstGame ? null : memberService.getMemberById(room.getCreatedUserId());
            val responseStartUser = memberMapper.toAllGameRoomResponse(startUser);
            val wordList = room.getWordList().stream().sorted(((o1, o2) -> o1.getId().compareTo(o2.getId()))).map(word -> {
                val member = memberService.getMemberById(word.getMemberId());
                val responseMember = memberMapper.toAllGameRoomResponse(member);
                return new WordChainGameRoomResponse.WordResponse(word.getWord(), responseMember);
            }).collect(Collectors.toList());
            return new WordChainGameRoomResponse(room.getId(), room.getStartWord(), responseStartUser, wordList);
        }).collect(Collectors.toList());
        val hasNextMember = (limit != null && rooms.size() > limit);
        if (hasNextMember) rooms.remove(rooms.size() - 1);
        val response = new WordChainGameAllResponse(roomList,hasNextMember);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "새 게임 생성")
    @PostMapping("/newGame")
    public ResponseEntity<WordChainGameGenerateResponse> createGameRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val member = memberService.getMemberById(memberDetails.getId());
        val newRoom = wordChainGameService.createWordGameRoom(member);
        val isFirstNewGame = newRoom.getCreatedUserId() == null;
        val responseMember = (isFirstNewGame) ? null : memberMapper.toUserResponse(member);
        val response = new WordChainGameGenerateResponse(newRoom.getId(), newRoom.getStartWord(), responseMember);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "명예의 전당 목록")
    @GetMapping("/winners")
    public ResponseEntity<WordChainGameWinnerAllResponse> getGameWinners(
        @RequestParam(required = false, name = "limit") Integer limit,
        @RequestParam(required = false, name = "cursor") Integer cursor
    ) {
        val winners = wordChainGameService.getAllWinner(checkLimitForPagination(limit), cursor);
        val hasNextMember = (limit != null && winners.size() > limit);
        if (hasNextMember) winners.remove(winners.size() - 1);
        val response = new WordChainGameWinnerAllResponse(winners,hasNextMember);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private Integer checkLimitForPagination(Integer limit) {
        val isLimitEmpty = (limit == null);
        return isLimitEmpty ? null : limit + 1;
    }
}