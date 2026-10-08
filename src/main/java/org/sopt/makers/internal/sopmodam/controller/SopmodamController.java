package org.sopt.makers.internal.sopmodam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamVoteRequest;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamQuestionListResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamVoteResponse;
import org.sopt.makers.internal.sopmodam.service.SopmodamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sopmodam")
@SecurityRequirement(name = "Authorization")
@Tag(name = "Sopmodam 관련 API", description = "솝모담 질문 투표·답변 관련 API")
public class SopmodamController {

    private final SopmodamService sopmodamService;

    @Operation(
        summary = "솝모담 투표 화면 조회 API",
        description = """
            회차의 질문 목록과 내 투표 상태를 조회합니다. 질문별 득표수는 내려주지 않습니다.
            - 활동 YB·OB만 조회할 수 있습니다. 사용자 유형은 회차의 기수 기준으로 판단합니다.
            - 투표 시작 전이면 400, 투표 기간이 끝난 뒤에는 canVote = false 로 조회됩니다.
            - dDay = (voteEndAt - 1초)의 날짜 - 오늘 날짜(KST). 투표 기간이 아니면 null 입니다.
            - questions 는 등록 순서(questionId 오름차순)입니다.
            """
    )
    @GetMapping("/rounds/{roundId}/questions")
    public ResponseEntity<SopmodamQuestionListResponse> getQuestions(
        @PathVariable("roundId") Long roundId,
        @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(sopmodamService.getQuestions(userId, roundId));
    }

    @Operation(
        summary = "솝모담 질문 투표 API",
        description = """
            회차의 질문 중 하나에 투표합니다.
            - 한 회차에 한 번만 투표할 수 있고, 투표한 뒤에는 바꿀 수 없습니다.
            - 투표 기간(voteStartAt <= now < voteEndAt)에만 투표할 수 있습니다.
            - 활동 YB·OB만 투표할 수 있습니다. 사용자 유형은 회차의 기수 기준으로 판단합니다.
            """
    )
    @PostMapping("/rounds/{roundId}/votes")
    public ResponseEntity<SopmodamVoteResponse> vote(
        @PathVariable("roundId") Long roundId,
        @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
        @RequestBody @Valid SopmodamVoteRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sopmodamService.vote(userId, roundId, request.questionId()));
    }
}
