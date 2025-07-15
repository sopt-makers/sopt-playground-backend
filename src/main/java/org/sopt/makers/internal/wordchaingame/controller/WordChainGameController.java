package org.sopt.makers.internal.wordchaingame.controller;

import com.slack.api.socket_mode.request.InteractiveEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.auth.AuthConfig;
import org.sopt.makers.internal.auth.security.authentication.MakersAuthentication;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformClient;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.wordchaingame.dto.response.*;
import org.sopt.makers.internal.wordchaingame.dto.request.WordChainGameGenerateRequest;
import org.sopt.makers.internal.common.util.InfiniteScrollUtil;
import org.sopt.makers.internal.internal.InternalMemberDetails;
import org.sopt.makers.internal.exception.WordChainGameHasWrongInputException;
import org.sopt.makers.internal.member.mapper.MemberMapper;
import org.sopt.makers.internal.member.service.MemberService;
import org.sopt.makers.internal.wordchaingame.service.WordChainGameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chainWordGame")
@SecurityRequirement(name = "Authorization")
@Tag(name = "끝말잇기 게임", description = "끝말잇기 게임과 관련 API들")
public class WordChainGameController {
    private final WordChainGameService wordChainGameService;
    private final MemberService memberService;
    private final MemberMapper memberMapper;
    private final InfiniteScrollUtil infiniteScrollUtil;

    @Operation(summary = "단어보내기 API")
    @PostMapping("/wordGame")
    public ResponseEntity<WordChainGameGenerateResponse> getPostMapping(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestBody WordChainGameGenerateRequest request
    ) {
        Member member = memberService.getMemberById(userId);
        WordChainGameGenerateUserResponse responseMember = wordChainGameService.getGenerateResponseMember(userId);
        wordChainGameService.createWord(member, request);

        WordChainGameGenerateResponse response = new WordChainGameGenerateResponse(request.roomId(), request.word(), responseMember);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "게임 전체 조회", description = "cursor : 처음에는 null 또는 0, 이후 방번호 증 마지막 room의 id")
    @GetMapping("/gameRoom")
    public ResponseEntity<WordChainGameAllResponse> getPostMapping(
            @RequestParam(required = false, name = "limit") Integer limit,
            @RequestParam(required = false, name = "cursor") Long cursor
    ) {
        val rooms = wordChainGameService.getAllRoom(infiniteScrollUtil.checkLimitForPagination(limit), cursor);
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
        val hasNextGame = infiniteScrollUtil.checkHasNextElement(limit,roomList);
        val response = new WordChainGameAllResponse(roomList,hasNextGame);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "새 게임 생성")
    @PostMapping("/newGame")
    public ResponseEntity<WordChainGameGenerateResponse> createGameRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        val member = memberService.getMemberById(userId);
        val newRoom = wordChainGameService.createWordGameRoom(member);
        val isFirstNewGame = newRoom.getCreatedUserId() == null;
        WordChainGameGenerateUserResponse responseMember = (isFirstNewGame) ? null : wordChainGameService.getGenerateResponseMember(userId);
        val response = new WordChainGameGenerateResponse(newRoom.getId(), newRoom.getStartWord(), responseMember);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "명예의 전당 목록")
    @GetMapping("/winners")
    public ResponseEntity<WordChainGameWinnerAllResponse> getGameWinners(
        @RequestParam(required = false, name = "limit") Integer limit,
        @RequestParam(required = false, name = "cursor") Integer cursor
    ) {
        val winners = wordChainGameService.getAllWinner(infiniteScrollUtil.checkLimitForPagination(limit), cursor);
        val hasNextWinner = infiniteScrollUtil.checkHasNextElement(limit, winners);
        val response = new WordChainGameWinnerAllResponse(winners,hasNextWinner);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}