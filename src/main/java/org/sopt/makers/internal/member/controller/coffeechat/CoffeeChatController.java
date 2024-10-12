package org.sopt.makers.internal.member.controller.coffeechat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.dto.CommonResponse;
import org.sopt.makers.internal.dto.member.CoffeeChatRequest;
import org.sopt.makers.internal.dto.member.CoffeeChatResponse;
import org.sopt.makers.internal.dto.member.CoffeeChatResponse.CoffeeChatVo;
import org.sopt.makers.internal.member.service.coffeechat.CoffeeChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/coffeechat")
public class CoffeeChatController {

    private final CoffeeChatService coffeeChatService;

    @Operation(summary = "커피챗 활성 유저 조회 API")
    @GetMapping("")
    public ResponseEntity<CoffeeChatResponse> getCoffeeChatList() {
        List<CoffeeChatVo> coffeeChatActivateMemberList = coffeeChatService.getCoffeeChatActivateMemberList();
        return ResponseEntity.status(HttpStatus.OK).body(new CoffeeChatResponse(coffeeChatActivateMemberList, coffeeChatActivateMemberList.size()));
    }


    @Operation(summary = "커피챗 수신 API")
    @PostMapping("")
    public ResponseEntity<CommonResponse> requestCoffeeChat(
            @RequestBody CoffeeChatRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        coffeeChatService.sendCoffeeChatRequest(request, memberDetails.getId());
        val response = new CommonResponse(true, "성공적으로 커피챗 이메일을 보냈습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
