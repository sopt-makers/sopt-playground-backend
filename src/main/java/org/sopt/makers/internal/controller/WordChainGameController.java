package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.dto.wordChainGame.WordChainGameAllResponse;
import org.sopt.makers.internal.dto.wordChainGame.WordChainGameGenerateRequest;
import org.sopt.makers.internal.dto.wordChainGame.WordChainGameGenerateResponse;
import org.sopt.makers.internal.dto.wordChainGame.WordChainGameRoomResponse;
import org.sopt.makers.internal.mapper.MemberMapper;
import org.sopt.makers.internal.mapper.WordChainGameMappper;
import org.sopt.makers.internal.service.MemberService;
import org.sopt.makers.internal.service.WordChainGameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "단어보내기 API")
    @PostMapping("/wordGame")
    public ResponseEntity<WordChainGameGenerateResponse> getPostMapping(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @RequestBody WordChainGameGenerateRequest request
    ) {
        val member = memberService.getMemberById(memberDetails.getId());
        val responseMember = memberMapper.toUserResponse(member);
        wordChainGameService.saveWord(member, request);
        val response = new WordChainGameGenerateResponse(request.roomId(), request.word(), responseMember);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "게임 전체 조회")
    @GetMapping("/gameRoom")
    public ResponseEntity<WordChainGameAllResponse> getPostMapping(
            @RequestParam(required = false, name = "limit") Integer limit,
            @RequestParam(required = false, name = "cursor") Integer cursor
    ) {
        val rooms = wordChainGameService.getAllRoom(checkLimitForPagination(limit), cursor);
        val roomList = rooms.stream().map(room -> {
            val wordList = room.getWordList().stream().map(word -> {
                val member = memberService.getMemberById(word.getMemberId());
                val responseMember = memberMapper.toAllGameRoomResponse(member);
                return new WordChainGameRoomResponse.WordResponse(word.getWord(), responseMember);
            }).collect(Collectors.toList());
            return new WordChainGameRoomResponse(room.getId(), wordList);
        }).collect(Collectors.toList());
        val hasNextMember = (limit != null && rooms.size() > limit);
        if (hasNextMember) rooms.remove(rooms.size() - 1);
        val response = new WordChainGameAllResponse(roomList,hasNextMember);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "새 게임 생성")
    @GetMapping("/newGame")
    public ResponseEntity<WordChainGameGenerateResponse> createGameRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val member = memberService.getMemberById(memberDetails.getId());
        val newRoom = wordChainGameService.createWordGameRoom(member);
        val responseMember = memberMapper.toUserResponse(member);
        val response = new WordChainGameGenerateResponse(newRoom.getId(), newRoom.getStartWord(), responseMember);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private Integer checkLimitForPagination(Integer limit) {
        val isLimitEmpty = (limit == null);
        return isLimitEmpty ? null : limit + 1;
    }
}