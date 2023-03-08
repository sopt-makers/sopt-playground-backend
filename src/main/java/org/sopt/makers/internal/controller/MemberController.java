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
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.mapper.MemberMapper;
import org.sopt.makers.internal.service.CoffeeChatService;
import org.sopt.makers.internal.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@SecurityRequirement(name = "Authorization")
@Tag(name = "Member 관련 API", description = "Member와 관련 API들")
public class MemberController {
    private final MemberService memberService;
    private final CoffeeChatService coffeeChatService;
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
    public ResponseEntity<List<MemberResponse>> getMemberByName (@RequestParam String name) {
        val members = memberService.getMemberByName(name);
        val responses = members.stream().map(memberMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @Operation(summary = "유저 프로필 생성 API")
    @PostMapping("/profile")
    public ResponseEntity<MemberProfileResponse> createUserProfile (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @RequestBody MemberProfileSaveRequest request
    ) {
        val currentCount = request.careers().stream().filter(c -> c.isCurrent()).count();
        if (currentCount > 1) throw new ClientBadRequestException("현재 직장이 2개 이상입니다.");
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
        val currentCount = request.careers().stream().filter(c -> c.isCurrent()).count();
        if (currentCount > 1) throw new ClientBadRequestException("현재 직장이 2개 이상입니다.");
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
        val memberProfileProjects = memberService.getMemberProfileProjects(id);
        val activityMap = memberService.getMemberProfileActivity(
                member.getActivities(),
                memberProfileProjects
        );
        val activityResponses = activityMap.entrySet().stream().map(entry ->
                new MemberProfileSpecificResponse.MemberActivityResponse(entry.getKey(), entry.getValue())
                ).collect(Collectors.toList());
        val isMine = Objects.equals(member.getId(), memberDetails.getId());
        val response = memberMapper.toProfileSpecificResponse(
                member, isMine, memberProfileProjects, activityResponses
        );
        sortProfileCareer(response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private void sortProfileCareer (MemberProfileSpecificResponse response) {
        response.careers().sort((a, b) -> {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            val start = YearMonth.parse(a.startDate(), formatter);
            val end = YearMonth.parse(b.startDate(), formatter);
            return end.compareTo(start);
        });
        MemberProfileSpecificResponse.MemberCareerResponse currentCareer = null;
        int index = 0;
        for (val career: response.careers()) {
            if (career.isCurrent()) {
                currentCareer = career;
                break;
            }
            index += 1;
        }
        if (currentCareer != null) {
            response.careers().add(0, currentCareer);
            response.careers().remove(index+1);
        }
    }

    @Operation(summary = "자신의 토큰으로 프로필 조회 API")
    @GetMapping("/profile/me")
    public ResponseEntity<MemberProfileSpecificResponse> getMyProfile (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val id = memberDetails.getId();
        val member = memberService.getMemberHasProfileById(id);
        val memberProfileProjects = memberService.getMemberProfileProjects(id);
        val activityMap = memberService.getMemberProfileActivity(
                member.getActivities(),
                memberProfileProjects
        );
        val activityResponses = activityMap.entrySet().stream().map(entry ->
                new MemberProfileSpecificResponse.MemberActivityResponse(entry.getKey(), entry.getValue())
        ).collect(Collectors.toList());
        val isMine = Objects.equals(member.getId(), memberDetails.getId());
        val response = memberMapper.toProfileSpecificResponse(
                member, isMine, memberProfileProjects, activityResponses
        );
        sortProfileCareer(response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "멤버 프로필 전체 조회 API",
            description =
                    """
                    filter 1 -> 기획 / 2 -> 디자인 / 3 -> 웹 / 4 -> 서버 / 5 -> 안드로이드 / 6 -> iOS, 
                    참고로 asc(오름차순)로 정렬되어 있음
                    """
    )
    @GetMapping("/profile")
    public ResponseEntity<List<MemberProfileResponse>> getUserProfiles (
            @RequestParam(required = false, name = "filter") Integer filter,
            @RequestParam(required = false, name = "limit") Integer limit,
            @RequestParam(required = false, name = "cursor") Integer cursor,
            @RequestParam(required = false, name = "name") String name
    ) {
        val members = memberService.getMemberProfiles(filter, limit, cursor, name);
        val memberList = members.stream().map(memberMapper::toProfileResponse).collect(Collectors.toList());

        val nextMember = memberService.getMemberProfiles(filter, limit, cursor + limit, name);
        val nextMemberList = nextMember.stream().map(memberMapper::toProfileResponse).toList();
        val hasNextMember = nextMemberList.size() > 0;

        val response = new MemberAllProfileResponse(memberList, hasNextMember);
        return ResponseEntity.status(HttpStatus.OK).body(response);
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

    @Operation(summary = "커피챗")
    @PostMapping("/coffeechat")
    public ResponseEntity<CommonResponse> requestCoffeeChat(
            @RequestBody CoffeeChatRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        coffeeChatService.sendCoffeeChatRequest(request, memberDetails.getId());
        val response = new CommonResponse(true, "성공적으로 커피챗 이메일을 보냈습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
