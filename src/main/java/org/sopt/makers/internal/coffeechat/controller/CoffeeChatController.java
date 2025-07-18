package org.sopt.makers.internal.coffeechat.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.sopt.makers.internal.internal.InternalMemberDetails;
import org.sopt.makers.internal.common.CommonResponse;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatReviewRequest;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatDetailResponse;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatHistoryTitleResponse;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatResponse;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatResponse.CoffeeChatVo;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatDetailsRequest;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatOpenRequest;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatRequest;
import org.sopt.makers.internal.coffeechat.dto.response.CoffeeChatReviewResponse;
import org.sopt.makers.internal.coffeechat.service.CoffeeChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Authorization")
@RequestMapping("/api/v1/members/coffeechat")
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
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String topicType,
            @RequestParam(required = false) String career,
            @RequestParam(required = false) String part,
            @RequestParam(required = false) String search
    ) {
        List<CoffeeChatVo> searchCoffeeChatList = coffeeChatService.getSearchCoffeeChatList(memberDetails.getId(), section, topicType, career, part, search);
        return ResponseEntity.status(HttpStatus.OK).body(new CoffeeChatResponse(searchCoffeeChatList));
    }

    @Operation(summary = "커피챗 상세 조회 API")
    @GetMapping("/{memberId}")
    public ResponseEntity<CoffeeChatDetailResponse> getCoffeeChat(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @PathVariable Long memberId
    ) {

        return ResponseEntity.status(HttpStatus.OK).body(coffeeChatService.getCoffeeChatDetail(memberDetails.getId(), memberId));
    }

    @Operation(summary = "커피챗/쪽지 수신 API")
    @PostMapping("")
    public ResponseEntity<CommonResponse> requestCoffeeChat(
            @Valid @RequestBody CoffeeChatRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        coffeeChatService.sendCoffeeChatRequest(request, memberDetails.getId());
        CommonResponse response = new CommonResponse(true, "커피챗/쪽지 수신 요청에 성공했습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "커피챗 공개/비공개 토글 API")
    @PatchMapping("/open")
    public ResponseEntity<CommonResponse> updateCoffeeChatActivate(
            @Valid @RequestBody CoffeeChatOpenRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        coffeeChatService.updateCoffeeChatOpen(memberDetails.getId(), request);
        CommonResponse response = new CommonResponse(true, "커피챗 공개 여부 변경에 성공했습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "커피챗 정보 생성 API")
    @PostMapping("/details")
    public ResponseEntity<CommonResponse> createCoffeeChatDetails(
            @Valid @RequestBody CoffeeChatDetailsRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        coffeeChatService.createCoffeeChatDetails(memberDetails.getId(), request);
        CommonResponse response = new CommonResponse(true, "커피챗 정보 생성에 성공했습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "커피챗 정보 수정 API")
    @PutMapping("/details")
    public ResponseEntity<CommonResponse> updateCoffeeChatDetails(
            @Valid @RequestBody CoffeeChatDetailsRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        coffeeChatService.updateCoffeeChatDetails(memberDetails.getId(), request);
        CommonResponse response = new CommonResponse(true, "커피챗 정보 수정에 성공했습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "커피챗 정보 삭제 API")
    @DeleteMapping("/details")
    public ResponseEntity<CommonResponse> deleteCoffeeChatDetails(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        coffeeChatService.deleteCoffeeChatDetails(memberDetails.getId());
        CommonResponse response = new CommonResponse(true, "커피챗 정보 삭제에 성공했습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "진행한 커피챗 타이틀 조회 API")
    @GetMapping("/history")
    public ResponseEntity<CoffeeChatHistoryTitleResponse> getCoffeeChatHistoryTitle(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {

        return ResponseEntity.status(HttpStatus.OK).body(new CoffeeChatHistoryTitleResponse(coffeeChatService.getCoffeeChatHistories(memberDetails.getId())));
    }

    @Operation(summary = "진행한 커피챗 리뷰 생성 API")
    @PostMapping("/review")
    public ResponseEntity<Map<String, Boolean>> reviewCoffeeChat(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @RequestBody @Valid CoffeeChatReviewRequest request
    ) {
        coffeeChatService.createCoffeeChatReview(memberDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
    }

    @Operation(summary = "커피챗 리뷰 조회 API")
    @GetMapping("/reviews")
    public ResponseEntity<CoffeeChatReviewResponse> getCoffeeChatReview() {

        return ResponseEntity.status(HttpStatus.OK).body(new CoffeeChatReviewResponse(coffeeChatService.getRecentCoffeeChatReviews()));
    }
}
