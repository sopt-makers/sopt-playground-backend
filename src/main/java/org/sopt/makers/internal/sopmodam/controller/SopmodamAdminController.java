package org.sopt.makers.internal.sopmodam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.popup.auth.AdminKeyValidator;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamRoundDetailResponse;
import org.sopt.makers.internal.sopmodam.service.SopmodamAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
