package org.sopt.makers.internal.sopmodam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamAnswerCreateRequest;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamAnswerUpdateRequest;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamVoteRequest;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamCurrentCardResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamAnswerCreateResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamAnswerListResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamAnswerUpdateResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamQuestionListResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamVoteResponse;
import org.sopt.makers.internal.sopmodam.service.SopmodamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
            - 최종 질문은 회차에 저장된 selectedQuestionId 로 조회합니다. 투표 종료 후 미선정 상태이면 단독 최다 득표 질문을 자동 선정하여 저장합니다. 동률이면 미확정 상태를 유지합니다.
            - selectedQuestion 은 VOTING 이 아닐 때, answerCount 는 REVEALED 일 때만 내려줍니다.
            - hasVoted, hasAnswered 는 단계와 관계없이 이번 회차의 실제 투표·답변 참여 여부입니다.
            - canVote 는 투표 단계의 투표 가능한 유형이고 아직 투표하지 않았을 때 true 입니다.
            - canAnswer 는 답변 모집 단계의 활동 OB·명예기수이면 true 입니다. 이미 답변했어도 수정할 수 있으므로 true 입니다.
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
        summary = "솝모담 공개 답변 목록 조회 API",
        description = """
            해당 회차의 공개된 답변 목록을 전체 조회합니다.
            - 최종 질문은 회차에 저장된 질문 ID로 조회합니다. 미선정이면 단독 최다 득표 질문을 자동 선정하여 저장합니다.
            - REVEALED 단계에서만 조회 가능합니다. 그 외 phase는 400입니다.
            - 로그인한 사용자라면 유형 무관 조회 가능합니다.
            - answers는 updatedAt 오름차순, 동일하면 answerId 오름차순으로 정렬됩니다.
            - writer.generation·part는 작성자의 가장 최근 SOPT·Makers 활동 기준입니다.
            - writer.profileImage는 없으면 null입니다.
            """
    )
    @GetMapping("/rounds/{roundId}/answers")
    public ResponseEntity<SopmodamAnswerListResponse> getAnswers(
        @PathVariable("roundId") Long roundId,
        @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(sopmodamService.getAnswers(roundId));
    }

    @Operation(
        summary = "솝모담 답변 작성 API",
        description = """
            회차의 최종 선정 질문에 답변을 작성합니다.
            - 최종 질문은 회차에 저장된 질문 ID로 조회합니다. 미선정이면 단독 최다 득표 질문을 자동 선정하여 저장합니다.
            - 답변 모집 기간(ANSWERING)에만 작성할 수 있습니다.
            - 회차당 1개의 답변만 허용됩니다.
            - 활동 OB·명예 회원만 답변할 수 있습니다. (활동 YB 불가)
            - content: 1자 이상 500자 이하, 공백·줄바꿈만으로 이루어진 경우 불가
            """
    )
    @PostMapping("/rounds/{roundId}/answers")
    public ResponseEntity<SopmodamAnswerCreateResponse> createAnswer(
        @PathVariable("roundId") Long roundId,
        @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
        @RequestBody @Valid SopmodamAnswerCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sopmodamService.createAnswer(userId, roundId, request));
    }

    @Operation(
        summary = "솝모담 답변 수정 API",
        description = """
            작성한 답변을 수정합니다.
            - 답변 모집 기간(ANSWERING)에만 수정할 수 있습니다.
            - 본인의 답변만 수정할 수 있습니다.
            - content: 1자 이상 500자 이하, 공백·줄바꿈만으로 이루어진 경우 불가
            """
    )
    @PutMapping("/answers/{answerId}")
    public ResponseEntity<SopmodamAnswerUpdateResponse> updateAnswer(
        @PathVariable("answerId") Long answerId,
        @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
        @RequestBody @Valid SopmodamAnswerUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(sopmodamService.updateAnswer(userId, answerId, request));
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
