package org.sopt.makers.internal.sopmodam.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.popup.auth.AdminKeyValidator;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamRoundCreateRequest;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamRoundUpdateRequest;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamRoundDetailResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamRoundSummaryResponse;
import org.sopt.makers.internal.sopmodam.service.SopmodamAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// admin-key 헤더가 없을 때 500 이 아니라 403 을 주도록 required = false 로 받고 AdminKeyValidator 가 검증한다
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/sopmodam")
@Tag(name = "Sopmodam 어드민 API", description = "솝모담 회차·질문 운영 API (admin-key 헤더 필수)")
public class SopmodamAdminController {

    private final SopmodamAdminService sopmodamAdminService;
    private final AdminKeyValidator adminKeyValidator;

    @Operation(
        summary = "솝모담 회차 생성 API",
        description = """
            회차를 질문과 함께 생성합니다. 응답은 회차 상세 조회와 같은 형식입니다.
            - 일정은 voteStartAt < voteEndAt < answerEndAt 순서여야 합니다. 모든 일시는 KST 이며 시작 포함·종료 미포함으로 판단합니다.
            - 마감 시각은 다음 날 00:00:00 으로 넣는 것을 권장합니다.
            - 다른 회차와 [voteStartAt, answerEndAt) 구간이 겹치면 409 입니다.
            - 질문은 2개 이상이어야 하고, 입력한 순서대로 등록됩니다.
            """
    )
    @PostMapping("/rounds")
    public ResponseEntity<SopmodamRoundDetailResponse> createRound(
        @Parameter(description = "어드민 키", required = true)
        @RequestHeader(value = "admin-key", required = false) String adminKey,
        @RequestBody @Valid SopmodamRoundCreateRequest request
    ) {
        adminKeyValidator.validate(adminKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(sopmodamAdminService.createRound(request));
    }

    @Operation(
        summary = "솝모담 회차 목록 조회 API",
        description = """
            모든 회차를 투표 시작 시각(voteStartAt) 내림차순으로 조회합니다. 페이지네이션은 없습니다.
            - phase: SCHEDULED(투표 시작 전) / VOTING / ANSWERING / REVEALED. 다음 회차가 시작된 지난 회차도 REVEALED 입니다.
            """
    )
    @GetMapping("/rounds")
    public ResponseEntity<List<SopmodamRoundSummaryResponse>> getRounds(
        @Parameter(description = "어드민 키", required = true)
        @RequestHeader(value = "admin-key", required = false) String adminKey
    ) {
        adminKeyValidator.validate(adminKey);
        return ResponseEntity.status(HttpStatus.OK).body(sopmodamAdminService.getRounds());
    }

    @Operation(
        summary = "솝모담 회차 상세 조회 API",
        description = """
            회차 정보와 질문별 득표수를 조회합니다.
            - phase: SCHEDULED(투표 시작 전) / VOTING / ANSWERING / REVEALED
            - needsSelection: 투표가 끝났는데 최다 득표가 동률이라 최종 질문이 정해지지 않았으면 true 입니다. 최종 질문을 직접 지정해야 합니다.
            - questions 는 등록 순서(questionId 오름차순)입니다.
            """
    )
    @GetMapping("/rounds/{roundId}")
    public ResponseEntity<SopmodamRoundDetailResponse> getRound(
        @Parameter(description = "어드민 키", required = true)
        @RequestHeader(value = "admin-key", required = false) String adminKey,
        @PathVariable("roundId") Long roundId
    ) {
        adminKeyValidator.validate(adminKey);
        return ResponseEntity.status(HttpStatus.OK).body(sopmodamAdminService.getRound(roundId));
    }

    @Operation(
        summary = "솝모담 회차 수정 API",
        description = """
            회차의 기수·주제·일정을 수정합니다. 질문은 이 API로 바꾸지 않습니다. 응답은 회차 상세 조회와 같은 형식입니다.
            - 모든 필드를 덮어씁니다. 바꾸지 않을 필드도 기존 값을 그대로 보내야 합니다.
            - 회차 생성과 같은 일정 순서 검증(400)과 겹침 검사(409)를 합니다. 수정하는 회차 자신과는 겹침 검사를 하지 않습니다.
            - 투표가 시작된 뒤에는 voteStartAt, 투표가 끝난 뒤에는 voteEndAt 을 바꿀 수 없습니다(400).
            """
    )
    @PutMapping("/rounds/{roundId}")
    public ResponseEntity<SopmodamRoundDetailResponse> updateRound(
        @Parameter(description = "어드민 키", required = true)
        @RequestHeader(value = "admin-key", required = false) String adminKey,
        @PathVariable("roundId") Long roundId,
        @RequestBody @Valid SopmodamRoundUpdateRequest request
    ) {
        adminKeyValidator.validate(adminKey);
        return ResponseEntity.status(HttpStatus.OK).body(sopmodamAdminService.updateRound(roundId, request));
    }
}
