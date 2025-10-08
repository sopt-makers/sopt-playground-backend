package org.sopt.makers.internal.coffeechat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatDetailsRequest;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatOpenRequest;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatRequest;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatReviewRequest;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatDetailResponse;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatHistoryTitleResponse;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatResponse;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatResponse.CoffeeChatVo;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatReviewResponse;
import org.sopt.makers.internal.coffeechat.service.CoffeeChatService;
import org.sopt.makers.internal.common.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Authorization")
@RequestMapping("/api/v1/members/coffeechat")
@Tag(name = "커피챗 관련 API")
public class CoffeeChatController {

    private final CoffeeChatService coffeeChatService;

    @Operation(summary = "최근 진행된 커피챗 유저 조회 API")
    @GetMapping("/recent")
    public ResponseEntity<CoffeeChatResponse> getRecentCoffeeChatList() {
        List<CoffeeChatVo> recentCoffeeChatList = coffeeChatService.getRecentCoffeeChatList();
        return ResponseEntity.status(HttpStatus.OK).body(new CoffeeChatResponse(recentCoffeeChatList));
    }

    @Operation(summary = "커피챗 유저 검색 API")
    @GetMapping("")
    public ResponseEntity<CoffeeChatResponse> getSearchCoffeeChatList(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String topicType,
            @RequestParam(required = false) String career,
            @RequestParam(required = false) String part,
            @RequestParam(required = false) String search
    ) {
        List<CoffeeChatVo> searchCoffeeChatList = coffeeChatService.getSearchCoffeeChatList(userId, section, topicType, career, part, search);
        return ResponseEntity.status(HttpStatus.OK).body(new CoffeeChatResponse(searchCoffeeChatList));
    }

    @Operation(summary = "커피챗 상세 조회 API")
    @GetMapping("/{memberId}")
    public ResponseEntity<CoffeeChatDetailResponse> getCoffeeChat(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long memberId
    ) {

        return ResponseEntity.status(HttpStatus.OK).body(coffeeChatService.getCoffeeChatDetail(userId, memberId));
    }

    @Operation(summary = "커피챗/쪽지 수신 API")
    @PostMapping("")
    public ResponseEntity<CommonResponse> requestCoffeeChat(
            @Valid @RequestBody CoffeeChatRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        coffeeChatService.sendCoffeeChatRequest(request, userId);
        CommonResponse response = new CommonResponse(true, "커피챗/쪽지 수신 요청에 성공했습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "커피챗 공개/비공개 토글 API")
    @PatchMapping("/open")
    public ResponseEntity<CommonResponse> updateCoffeeChatActivate(
            @Valid @RequestBody CoffeeChatOpenRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        coffeeChatService.updateCoffeeChatOpen(userId, request);
        CommonResponse response = new CommonResponse(true, "커피챗 공개 여부 변경에 성공했습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "커피챗 정보 생성 API")
    @PostMapping("/details")
    public ResponseEntity<CommonResponse> createCoffeeChatDetails(
            @Valid @RequestBody CoffeeChatDetailsRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        coffeeChatService.createCoffeeChatDetails(userId, request);
        CommonResponse response = new CommonResponse(true, "커피챗 정보 생성에 성공했습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "커피챗 정보 수정 API")
    @PutMapping("/details")
    public ResponseEntity<CommonResponse> updateCoffeeChatDetails(
            @Valid @RequestBody CoffeeChatDetailsRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        coffeeChatService.updateCoffeeChatDetails(userId, request);
        CommonResponse response = new CommonResponse(true, "커피챗 정보 수정에 성공했습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "커피챗 정보 삭제 API")
    @DeleteMapping("/details")
    public ResponseEntity<CommonResponse> deleteCoffeeChatDetails(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        coffeeChatService.deleteCoffeeChatDetails(userId);
        CommonResponse response = new CommonResponse(true, "커피챗 정보 삭제에 성공했습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "진행한 커피챗 타이틀 조회 API")
    @GetMapping("/history")
    public ResponseEntity<CoffeeChatHistoryTitleResponse> getCoffeeChatHistoryTitle(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        CoffeeChatHistoryTitleResponse response = new CoffeeChatHistoryTitleResponse(coffeeChatService.getCoffeeChatHistories(userId));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "진행한 커피챗 리뷰 생성 API")
    @PostMapping("/review")
    public ResponseEntity<Map<String, Boolean>> reviewCoffeeChat(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestBody @Valid CoffeeChatReviewRequest request
    ) {
        coffeeChatService.createCoffeeChatReview(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
    }

    @Operation(summary = "커피챗 리뷰 조회 API")
    @GetMapping("/reviews")
    public ResponseEntity<CoffeeChatReviewResponse> getCoffeeChatReview() {

        return ResponseEntity.status(HttpStatus.OK).body(new CoffeeChatReviewResponse(coffeeChatService.getRecentCoffeeChatReviews()));
    }
}
