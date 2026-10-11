package org.sopt.makers.internal.sopmodam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamVoteRequest;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamCurrentCardResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamMyAnswerResponse;
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
        summary = "솝모담 홈 카드 조회 API",
        description = """
            커뮤니티 홈에 보여줄 솝모담 카드를 조회합니다. 카드 유형과 버튼 활성화 여부는 서버가 정합니다.
            - 현재 회차는 투표가 시작된 회차 중 가장 늦게 시작한 회차입니다.
            - cardType 은 진행 단계(phase)와 사용자 유형으로 정합니다. 사용자 유형은 회차의 기수 기준으로 판단합니다.
              - VOTING: 활동 YB·OB → VOTE
              - ANSWERING: 활동 YB → ANSWER_PREPARING, 활동 OB·명예기수 → ANSWER_COLLECTING
              - REVEALED: 활동 YB·OB·명예기수 → REVEALED
            - dDay = (마감시각 - 1초)의 날짜 - 오늘 날짜(KST). VOTING 은 voteEndAt, ANSWERING 은 answerEndAt 기준이고 REVEALED 는 null 입니다.
            - selectedQuestion 은 VOTING 이 아닐 때, answerCount 는 REVEALED 일 때만 내려줍니다.
            - canVote, canAnswer 는 지금 버튼을 누를 수 있는지 여부입니다. 이미 참여했으면 false 입니다.
            - 보여줄 카드가 없으면 204 입니다.
              - 현재 회차가 없는 경우
              - VOTING 단계의 명예기수, 회차 기수보다 뒤 기수인 회원
              - VOTING 이후 단계인데 최종 질문이 정해지지 않은 경우
            """
    )
    @GetMapping("/current")
    public ResponseEntity<SopmodamCurrentCardResponse> getCurrentCard(
        @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        return sopmodamService.getCurrentCard(userId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

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

    @Operation(
        summary = "솝모담 내 답변 조회 API",
        description = """
            회차의 최종 질문과 내 답변을 조회합니다. 답변 작성·수정 화면에서 사용합니다.
            - 활동 OB·명예기수만 조회할 수 있습니다. 사용자 유형은 회차의 기수 기준으로 판단합니다.
            - 투표가 끝나기 전이거나 최종 질문이 정해지지 않았으면 400 입니다.
            - answer 는 아직 작성하지 않았으면 null 입니다.
            - canEdit 은 답변 모집 기간(voteEndAt <= now < answerEndAt)일 때 true 입니다.
            """
    )
    @GetMapping("/rounds/{roundId}/answers/me")
    public ResponseEntity<SopmodamMyAnswerResponse> getMyAnswer(
        @PathVariable("roundId") Long roundId,
        @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(sopmodamService.getMyAnswer(userId, roundId));
    }
}
