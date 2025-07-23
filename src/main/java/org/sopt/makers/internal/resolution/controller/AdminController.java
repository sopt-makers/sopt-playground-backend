package org.sopt.makers.internal.resolution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.resolution.service.LuckyPickService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "관리자 API", description = "관리자 전용 API List")
public class AdminController {
    private final LuckyPickService luckyPickService;

    @Operation(summary = "행운 뽑기 이벤트 준비 및 추첨 실행")
    @PostMapping("/lucky-pick/prepare")
    public ResponseEntity<String> prepareLuckyPickEvent(
    ) {
        luckyPickService.prepareLuckyPickEvent();
        return ResponseEntity.ok("행운 뽑기 이벤트 준비 및 추첨이 성공적으로 완료되었습니다.");
    }
}
