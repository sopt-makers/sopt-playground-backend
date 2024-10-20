package org.sopt.makers.internal.member.controller.coffeechat;

import java.util.List;

import javax.validation.Valid;

import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.dto.CommonResponse;
import org.sopt.makers.internal.member.controller.coffeechat.dto.response.CoffeeChatDetailResponse;
import org.sopt.makers.internal.member.controller.coffeechat.dto.response.RecentCoffeeChatResponse;
import org.sopt.makers.internal.member.controller.coffeechat.dto.response.RecentCoffeeChatResponse.RecentCoffeeChat;
import org.sopt.makers.internal.member.dto.request.CoffeeChatDetailsRequest;
import org.sopt.makers.internal.member.dto.request.CoffeeChatOpenRequest;
import org.sopt.makers.internal.member.dto.request.CoffeeChatRequest;
import org.sopt.makers.internal.member.dto.response.CoffeeChatResponse;
import org.sopt.makers.internal.member.dto.response.CoffeeChatResponse.CoffeeChatVo;
import org.sopt.makers.internal.member.service.coffeechat.CoffeeChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/coffeechat")
public class CoffeeChatController {

    private final CoffeeChatService coffeeChatService;

    @Operation(summary = "최근 진행된 커피챗 유저 조회 API")
    @GetMapping("/recent")
    public ResponseEntity<RecentCoffeeChatResponse> getRecentCoffeeChatList() {
        List<RecentCoffeeChat> recentCoffeeChatList = coffeeChatService.getRecentCoffeeChatList();
        return ResponseEntity.status(HttpStatus.OK).body(new RecentCoffeeChatResponse(recentCoffeeChatList));
    }

    @Operation(summary = "커피챗 활성 유저 조회 API")
    @GetMapping("")
    public ResponseEntity<CoffeeChatResponse> getCoffeeChatList() {
        List<CoffeeChatVo> coffeeChatActivateMemberList = coffeeChatService.getCoffeeChatActivateMemberList();
        return ResponseEntity.status(HttpStatus.OK).body(new CoffeeChatResponse(coffeeChatActivateMemberList, coffeeChatActivateMemberList.size()));
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
}
