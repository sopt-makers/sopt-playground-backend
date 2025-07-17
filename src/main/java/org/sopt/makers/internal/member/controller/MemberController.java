package org.sopt.makers.internal.member.controller;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.sopt.makers.internal.common.util.InfiniteScrollUtil;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.enums.ActivityTeam;
import org.sopt.makers.internal.internal.InternalMemberDetails;
import org.sopt.makers.internal.common.CommonResponse;
import org.sopt.makers.internal.member.dto.request.CheckActivityRequest;
import org.sopt.makers.internal.member.dto.response.MemberAllProfileResponse;
import org.sopt.makers.internal.member.dto.response.MemberBlockResponse;
import org.sopt.makers.internal.member.dto.request.MemberBlockRequest;
import org.sopt.makers.internal.member.dto.request.MemberReportRequest;
import org.sopt.makers.internal.member.dto.response.MemberCrewResponse;
import org.sopt.makers.internal.member.dto.MemberProfileProjectVo;
import org.sopt.makers.internal.member.dto.response.MemberProfileResponse;
import org.sopt.makers.internal.member.dto.request.MemberProfileSaveRequest;
import org.sopt.makers.internal.member.dto.response.MemberProfileSpecificResponse;
import org.sopt.makers.internal.member.dto.request.MemberProfileUpdateRequest;
import org.sopt.makers.internal.member.dto.response.MemberResponse;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.external.makers.MakersCrewClient;
import org.sopt.makers.internal.member.mapper.MemberMapper;
import org.sopt.makers.internal.member.dto.response.MemberInfoResponse;
import org.sopt.makers.internal.member.dto.response.MemberPropertiesResponse;
import org.sopt.makers.internal.member.mapper.MemberResponseMapper;
import org.sopt.makers.internal.coffeechat.service.CoffeeChatService;
import org.sopt.makers.internal.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@SecurityRequirement(name = "Authorization")
@Tag(name = "Member 관련 API", description = "Member와 관련 API들")
public class MemberController {
    private final MemberService memberService;
    private final CoffeeChatService coffeeChatService;
    private final MemberMapper memberMapper;
    private final MemberResponseMapper memberResponseMapper;
    private final InfiniteScrollUtil infiniteScrollUtil;
    private final MakersCrewClient makersCrewClient;

    @Operation(summary = "유저 id로 조회 API")
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMember (@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.getMemberResponseById(id));
    }

    @Operation(summary = "자신의 토큰으로 조회 API")
    @GetMapping("/me")
    public ResponseEntity<MemberInfoResponse> getMyInformation (
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        MemberInfoResponse response = memberService.getMyInformation(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "유저 이름으로 조회 API")
    @GetMapping("/search")
    public ResponseEntity<List<MemberResponse>> getMemberByName (@RequestParam String name) {
        val members = memberService.getMemberByName(name);
        val responses = members.stream().map(memberMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }


    @Operation(summary = "유저 프로필 생성 API",
            description =
                    """
                        주량 : Double 
                        0 -> 못마셔요 / 0.5 -> 0.5병 / 1.0 -> 1병 / 1.5 -> 1.5병 /
                        2.0 -> 2병 / 2.5 -> 2.5병 / 3.0 -> 3병 이상               
                    """
    )
    @PostMapping("/profile")
    public ResponseEntity<MemberProfileResponse> createUserProfile (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @Valid @RequestBody MemberProfileSaveRequest request
    ) {
        val normalTeamNameRequest = request.activities().stream().filter(activity ->
                ActivityTeam.hasActivityTeam(activity.team())).count();
        if (request.activities().size() != normalTeamNameRequest) {
            throw new ClientBadRequestException("잘못된 솝트 활동 팀 이름입니다.");
        }
        val currentCount = request.careers().stream().filter(c -> c.isCurrent()).count();
        if (currentCount > 1) throw new ClientBadRequestException("현재 직장이 2개 이상입니다.");
        val member = memberService.saveMemberProfile(memberDetails.getId(), request);
        val isCoffeeChatActivate = coffeeChatService.getCoffeeChatActivate(member.getId());
        val response = memberMapper.toProfileResponse(member, isCoffeeChatActivate);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "멤버 프로필 수정 API",
            description =
                    """
                        주량 : Double 
                        0 -> 못마셔요 / 0.5 -> 0.5병 / 1.0 -> 1병 / 1.5 -> 1.5병 /
                        2.0 -> 2병 / 2.5 -> 2.5병 / 3.0 -> 3병 이상               
                    """
    )
    @PutMapping("/profile")
    public ResponseEntity<MemberProfileResponse> updateUserProfile (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @Valid @RequestBody MemberProfileUpdateRequest request
    ) {
        val normalTeamNameRequest = request.activities().stream().filter(activity ->
                ActivityTeam.hasActivityTeam(activity.team())).count();
        if (request.activities().size() != normalTeamNameRequest) {
            throw new ClientBadRequestException("잘못된 솝트 활동 팀 이름입니다.");
        }
        val currentCount = request.careers().stream().filter(c -> c.isCurrent()).count();
        if (currentCount > 1) throw new ClientBadRequestException("현재 직장이 2개 이상입니다.");
        val member = memberService.updateMemberProfile(memberDetails.getId(), request);
        val isCoffeeChatActivate = coffeeChatService.getCoffeeChatActivate(member.getId());
        val response = memberMapper.toProfileResponse(member, isCoffeeChatActivate);
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
        val soptActivity = memberService.getMemberProfileProjects(
                member.getActivities(),
                memberProfileProjects
        );
        val soptActivityResponse = soptActivity.stream()
                .map(m -> new MemberProfileProjectVo(m.id(), m.generation(), m.part(), checkTeamNullCondition(m.team()), m.projects()))
                .collect(Collectors.toList());
        val activityResponses = activityMap.entrySet().stream().map(entry ->
                new MemberProfileSpecificResponse.MemberActivityResponse(entry.getKey(), entry.getValue())
                ).collect(Collectors.toList());
        val isMine = Objects.equals(member.getId(), memberDetails.getId());
        val isCoffeeChatActivate = coffeeChatService.getCoffeeChatActivate(member.getId());
        val response = MemberProfileSpecificResponse.applyPhoneMasking(
            memberMapper.toProfileSpecificResponse(
                member, true, memberProfileProjects, activityResponses, soptActivityResponse, isCoffeeChatActivate
            ),
            isMine, isCoffeeChatActivate);
        sortProfileCareer(response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
        val soptActivityResponse = memberService.getMemberProfileProjects(
                member.getActivities(),
                memberProfileProjects
        );
        val isMine = Objects.equals(member.getId(), memberDetails.getId());
        val isCoffeeChatActivate = coffeeChatService.getCoffeeChatActivate(member.getId());
        val response = memberMapper.toProfileSpecificResponse(
                member, isMine, memberProfileProjects, activityResponses, soptActivityResponse, isCoffeeChatActivate
        );
        sortProfileCareer(response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "멤버 프로필 전체 조회 API",
            description =
                    """
                    filter :
                    1 -> 기획 / 2 -> 디자인 / 3 -> 웹 / 4 -> 서버 / 5 -> 안드로이드 / 6 -> iOS,
                    참고로 asc(오름차순)로 정렬되어 있음 \n
                    search :
                    이름 / 학교명 / 회사명 중에 속하는 문자열로 검색 \n
                    employed :\s
                    0 -> 무직/휴직중 / 1 -> 재직중 \n
                    orderBy :
                    1 -> 최근에 등록했순 / 2 -> 예전에 등록했순 / 3 -> 최근에 활동했순 / 4 -> 예전에 활동했순 \n
                    team :
                    임원진, 운영팀, 미디어팀, 메이커스
                    """
    )
    @GetMapping("/profile")
    public ResponseEntity<MemberAllProfileResponse> getUserProfiles (
            @RequestParam(required = false, name = "filter") Integer filter,
            @RequestParam(required = false, name = "limit") Integer limit,
            @RequestParam(required = false, name = "cursor") Integer cursor,
            @RequestParam(required = false, name = "search") String search,
            @RequestParam(required = false, name = "generation") Integer generation,
            @RequestParam(required = false, name = "employed") Integer employed,
            @RequestParam(required = false, name = "orderBy") Integer orderBy,
            @RequestParam(required = false, name = "mbti") String mbti,
            @RequestParam(required = false, name = "team") String team
    ) {
        val members = memberService.getMemberProfiles(filter, infiniteScrollUtil.checkLimitForPagination(limit), cursor, search, generation, employed, orderBy, mbti, team);
        val memberList = members.stream().map(member ->
                MemberProfileResponse.checkIsBlindPhone(
                        memberMapper.toProfileResponse(member, coffeeChatService.getCoffeeChatActivate(member.getId())),
                        memberMapper.mapPhoneIfBlind(member.getIsPhoneBlind(), member.getPhone()))
        ).collect(Collectors.toList());
        val hasNextMember = infiniteScrollUtil.checkHasNextElement(limit, memberList);
        val totalMembersCount = memberService.getMemberProfilesCount(filter, search, generation, employed, mbti, team);
        val response = new MemberAllProfileResponse(memberList, hasNextMember, totalMembersCount);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "본인 활동 기수 확인 여부 API",
            description = "해당 API를 호출하면 유저의 editActivitiesAble이 false로 바뀝니다"
    )
    @PutMapping("/activity/check")
    public ResponseEntity<Map<String, Boolean>> isOkayActivities(
            @RequestBody @Valid final CheckActivityRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        memberService.checkActivities(memberDetails.getId(), request.isCheck());
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("유저 기수 확인 여부가 변경됐습니다.", true));
    }

    @Operation(summary = "멤버 크루 조회 API")
    @GetMapping("/crew/{id}")
    public ResponseEntity<MemberCrewResponse> getUserCrew(
            @PathVariable Long id,
            @RequestParam(required = false, name = "page") Integer page,
            @RequestParam(required = false, name = "take") Integer take
    ) {
        val response = makersCrewClient.getUserAllCrew(page, take, id);
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

    @Operation(summary = "유저 차단 활성하기 API")
    @PatchMapping("/block/activate")
    public ResponseEntity<Map<String, Boolean>> blockUser (
            @RequestBody MemberBlockRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        memberService.blockUser(memberDetails.getId(), request.blockedMemberId());
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("유저 차단 활성 성공", true));
    }

    @Operation(summary = "유저 차단 여부 조회하기 API")
    @GetMapping("/block/{memberId}")
    public ResponseEntity<MemberBlockResponse> getUserBlockStatus (
            @PathVariable Long memberId,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val response = memberService.getBlockStatus(memberDetails.getId(), memberId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "유저 신고하기 API")
    @PostMapping("/report")
    public ResponseEntity<Map<String, Boolean>> reportUser (
            @RequestBody MemberReportRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        memberService.reportUser(memberDetails.getId(), request.reportMemberId());
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("유저 신고 성공", true));
    }

    @Operation(summary = "Amplitude 를 위한 user properties 반환 API ")
    @GetMapping("/property")
    public ResponseEntity<MemberPropertiesResponse> getUserProperty (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.getMemberProperties(memberDetails.getId()));
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

    private String checkTeamNullCondition (String team) {
        val teamNullCondition = (team == null || team.equals("해당 없음"));
        if (teamNullCondition) {
            team = null;
        }
        return team;
    }
}