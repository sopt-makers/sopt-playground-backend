package org.sopt.makers.internal.wordchaingame.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.common.util.InfiniteScrollUtil;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.MemberSimpleResonse;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.service.MemberService;
import org.sopt.makers.internal.wordchaingame.domain.WordChainGameRoom;
import org.sopt.makers.internal.wordchaingame.dto.request.WordChainGameGenerateRequest;
import org.sopt.makers.internal.wordchaingame.dto.response.WordChainGameAllResponse;
import org.sopt.makers.internal.wordchaingame.dto.response.WordChainGameGenerateResponse;
import org.sopt.makers.internal.wordchaingame.dto.response.WordChainGameRoomResponse;
import org.sopt.makers.internal.wordchaingame.dto.response.WordChainGameWinnerAllResponse;
import org.sopt.makers.internal.wordchaingame.service.WordChainGameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chainWordGame")
@SecurityRequirement(name = "Authorization")
@Tag(name = "끝말잇기 게임", description = "끝말잇기 게임과 관련 API들")
public class WordChainGameController {
    private final WordChainGameService wordChainGameService;
    private final MemberService memberService;
    private final PlatformService platformService;
    private final InfiniteScrollUtil infiniteScrollUtil;

    @Operation(summary = "단어보내기 API")
    @PostMapping("/wordGame")
    public ResponseEntity<WordChainGameGenerateResponse> getAllGameRooms(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestBody WordChainGameGenerateRequest request
    ) {
        Member member = memberService.getMemberById(userId);
        MemberSimpleResonse responseMember = platformService.getMemberSimpleInfo(userId);
        wordChainGameService.createWord(member, request);

        WordChainGameGenerateResponse response = new WordChainGameGenerateResponse(request.roomId(), request.word(), responseMember);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "게임 전체 조회", description = "cursor : 처음에는 null 또는 0, 이후 방번호 증 마지막 room의 id")
    @GetMapping("/gameRoom")
    public ResponseEntity<WordChainGameAllResponse> getAllGameRooms(
            @RequestParam(required = false, name = "limit") Integer limit,
            @RequestParam(required = false, name = "cursor") Long cursor
    ) {
        List<WordChainGameRoom> rooms = wordChainGameService.getAllRoom(infiniteScrollUtil.checkLimitForPagination(limit), cursor);
        Map<Long, InternalUserDetails> startUserMap = wordChainGameService.getUserMapFromCreatedUserIds(rooms);;

        List<WordChainGameRoomResponse> roomList = rooms.stream().map(room -> wordChainGameService.toRoomResponse(room, startUserMap)).toList();
        boolean hasNextGame = infiniteScrollUtil.checkHasNextElement(limit, roomList);

        WordChainGameAllResponse response = new WordChainGameAllResponse(roomList, hasNextGame);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "새 게임 생성")
    @PostMapping("/newGame")
    public ResponseEntity<WordChainGameGenerateResponse> createGameRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        Member member = memberService.getMemberById(userId);
        WordChainGameRoom newRoom = wordChainGameService.createWordGameRoom(member);
        boolean isFirstNewGame = newRoom.getCreatedUserId() == null;
        MemberSimpleResonse responseMember = (isFirstNewGame) ? null : platformService.getMemberSimpleInfo(userId);
        WordChainGameGenerateResponse response = new WordChainGameGenerateResponse(newRoom.getId(), newRoom.getStartWord(), responseMember);
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
        val response = new WordChainGameWinnerAllResponse(winners, hasNextWinner);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}