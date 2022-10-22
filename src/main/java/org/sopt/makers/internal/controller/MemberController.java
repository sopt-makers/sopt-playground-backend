package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.dto.member.MemberResponse;
import org.sopt.makers.internal.dto.member.MemberSaveRequest;
import org.sopt.makers.internal.mapper.MemberMapper;
import org.sopt.makers.internal.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "Authorization")
@Tag(name = "Member 관련 API", description = "Member와 관련 API들")
public class MemberController {
    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @Operation(summary = "유저 id로 조회 API")
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMember (@PathVariable Long id) {
        val member = memberService.getMemberById(id);
        val response = memberMapper.toResponse(member);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "유저 id로 조회 API")
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInformation (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val member = memberService.getMemberById(memberDetails.getId());
        val response = memberMapper.toResponse(member);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "유저 이름으로 조회 API")
    @GetMapping("/search")
    public ResponseEntity<MemberResponse> getMemberByName (@RequestParam String name) {
        val member = memberService.getMemberByName(name);
        val response = memberMapper.toResponse(member);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "유저 생성 API")
    @PostMapping("")
    public ResponseEntity<MemberResponse> getMemberByName (@RequestBody MemberSaveRequest request) {
        val member = memberService.createMember(request);
        val response = memberMapper.toResponse(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
