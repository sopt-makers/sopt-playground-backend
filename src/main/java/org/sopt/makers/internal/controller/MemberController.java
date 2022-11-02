package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.dto.CommonResponse;
import org.sopt.makers.internal.dto.member.*;
import org.sopt.makers.internal.mapper.MemberMapper;
import org.sopt.makers.internal.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Operation(summary = "자신의 토큰으로 조회 API")
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

    @Operation(summary = "유저 프로필 생성 API")
    @PostMapping("/profile")
    public ResponseEntity<MemberProfileResponse> createUserProfile (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @RequestBody MemberProfileSaveRequest request
    ) {
        val member = memberService.saveMemberProfile(memberDetails.getId(), request);
        val response = memberMapper.toProfileResponse(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "멤버 프로필 수정 API")
    @PutMapping("/profile")
    public ResponseEntity<MemberProfileResponse> updateUserProfile (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @RequestBody MemberProfileUpdateRequest request
    ) {
        val member = memberService.updateMemberProfile(memberDetails.getId(), request);
        val response = memberMapper.toProfileResponse(member);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "멤버 프로필 조회 API")
    @GetMapping("/profile/{id}")
    public ResponseEntity<MemberProfileSpecificResponse> getUserProfile (
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val member = memberService.getMemberHasProfileById(id);
        val isMine = Objects.equals(member.getId(), memberDetails.getId());
        val response = memberMapper.toProfileSpecificResponse(member, isMine);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "자신의 토큰으로 프로필 조회 API")
    @GetMapping("/profile/me")
    public ResponseEntity<MemberProfileResponse> getMyProfile (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val member = memberService.getMemberHasProfileById(memberDetails.getId());
        val response = memberMapper.toProfileResponse(member);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "멤버 프로필 전체 조회 API")
    @GetMapping("/profile")
    public ResponseEntity<List<MemberProfileResponse>> getUserProfiles () {
        val members = memberService.getMemberProfiles();
        val responses = members.stream().map(memberMapper::toProfileResponse).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @Operation(summary = "멤버 프로필 링크 삭제 API")
    @DeleteMapping("/profile/link/{linkId}")
    public ResponseEntity<CommonResponse> deleteUserProfileLink (
            @PathVariable(name = "linkId") Long linkId,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        memberService.deleteUserProfileLink(linkId, memberDetails.getId());
        val response = new CommonResponse(true, "성공적으로 link를 삭제했습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "멤버 프로필 활동 삭제 API")
    @DeleteMapping("/profile/activity/{activityId}")
    public ResponseEntity<CommonResponse> deleteUserProfileActivity (
            @PathVariable(name = "activityId") Long activityId,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        memberService.deleteUserProfileActivity(activityId, memberDetails.getId());
        val response = new CommonResponse(true, "성공적으로 activity를 삭제했습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
