package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.config.AuthConfig;
import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.domain.MemberSoptActivity;
import org.sopt.makers.internal.domain.Project;
import org.sopt.makers.internal.dto.internal.*;
import org.sopt.makers.internal.dto.project.ProjectLinkDao;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.mapper.MemberMapper;
import org.sopt.makers.internal.mapper.ProjectResponseMapper;
import org.sopt.makers.internal.service.InternalApiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/api/v1")
@SecurityRequirement(name = "Authorization")
@Slf4j
@Tag(name = "내부 서비스 오픈 API")
public class InternalOpenApiController {

    private final InternalApiService internalApiService;
    private final ProjectResponseMapper projectMapper;
    private final MemberMapper memberMapper;

    private final AuthConfig authConfig;
    private final List<String> organizerPartName = List.of(
            "운영 팀장", "미디어 팀장", "총무", "회장", "부회장", "웹 파트장", "기획 파트장",
            "서버 파트장", "디자인 파트장", "안드로이드 파트장", "iOS 파트장", "메이커스 리드");

    @Operation(summary = "Project id로 조회 API")
    @GetMapping("/projects/{id}")
    public ResponseEntity<InternalProjectDetailResponse> getProject (@PathVariable Long id) {
        val projectMembers = internalApiService.fetchById(id);
        val projectLinks = internalApiService.fetchLinksById(id);
        val response = projectMapper.toInternalProjectDetailResponse(projectMembers, projectLinks);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Project 전체 조회 API")
    @GetMapping("/projects")
    public ResponseEntity<List<InternalProjectResponse>> getProjects () {
        val projectMap = internalApiService.fetchAll().stream()
                .collect(Collectors.toMap(Project::getId, Function.identity()));
        val projectLinkMap = internalApiService.fetchAllLinks().stream()
                .collect(Collectors.groupingBy(ProjectLinkDao::id, Collectors.toList()));
        val projectIds = projectMap.keySet();
        val responses = projectIds.stream()
                .map(id -> projectMapper.toInternalProjectResponse(projectMap.get(id), projectLinkMap.getOrDefault(id, List.of())))
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @Operation(summary = "자신의 토큰으로 조회 API")
    @GetMapping("/members/me")
    public ResponseEntity<InternalMemberResponse> getMyInformation (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val member = internalApiService.getMemberById(memberDetails.getId());
        val response = memberMapper.toInternalResponse(member);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "자신의 토큰으로 프로필 조회 API")
    @GetMapping("/members/profile/me")
    public ResponseEntity<InternalMemberProfileSpecificResponse> getMyProfile (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val id = memberDetails.getId();
        val member = internalApiService.getMemberHasProfileById(id);
        val memberProfileProjects = internalApiService.getMemberProfileProjects(id);
        val activityMap = internalApiService.getMemberProfileActivity(
                member.getActivities(),
                memberProfileProjects
        );
        val activityResponses = activityMap.entrySet().stream().map(entry ->
                new InternalMemberProfileSpecificResponse.MemberActivityResponse(entry.getKey(), entry.getValue())
        ).collect(Collectors.toList());
        val isMine = Objects.equals(member.getId(), memberDetails.getId());
        val response = memberMapper.toInternalProfileSpecificResponse(
                member, isMine, memberProfileProjects, activityResponses
        );
        sortProfileCareer(response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private void sortProfileCareer (InternalMemberProfileSpecificResponse response) {
        response.careers().sort((a, b) -> {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            val start = YearMonth.parse(a.startDate(), formatter);
            val end = YearMonth.parse(b.startDate(), formatter);
            return end.compareTo(start);
        });
        InternalMemberProfileSpecificResponse.MemberCareerResponse currentCareer = null;
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

    @Operation(
            summary = "멤버 프로필 전체 조회 API",
            description =
                    """
                    filter 1 -> 기획 / 2 -> 디자인 / 3 -> 웹 / 4 -> 서버 / 5 -> 안드로이드 / 6 -> iOS,
                    참고로 asc(오름차순)로 정렬되어 있음
                    """
    )
    @GetMapping("/profile")
    public ResponseEntity<InternalMemberAllProfileResponse> getUserProfiles (
            @RequestParam(required = false, name = "filter") Integer filter,
            @RequestParam(required = false, name = "limit") Integer limit,
            @RequestParam(required = false, name = "cursor") Integer cursor,
            @RequestParam(required = false, name = "name") String name,
            @RequestParam(required = false, name = "generation") Integer generation
    ) {
        val members = limit == null ? internalApiService.getMemberProfiles(filter, limit, cursor, name, generation) : internalApiService.getMemberProfiles(filter, limit + 1, cursor, name, generation);
        val memberList = members.stream().map(memberMapper::toInternalProfileResponse).collect(Collectors.toList());
        val hasNextMember = (limit != null && memberList.size() > limit);
        if (hasNextMember) memberList.remove(members.size() - 1);
        val response = new InternalMemberAllProfileResponse(memberList, hasNextMember);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "공홈팀 멤버 프로필 조회 API",
            description =
                    """
                    filter 1 -> 기획 / 2 -> 디자인 / 3 -> 웹 / 4 -> 서버 / 5 -> 안드로이드 / 6 -> iOS
                    """
    )
    @GetMapping("/official/members/profile")
    public ResponseEntity<InternalAllOfficialMemberResponse> getMemberProfilesByGenerationAndPart (
            @RequestParam(required = false, name = "filter") Integer filter,
            @RequestParam(name = "generation") Integer generation
    ) {
        if (generation == null) throw new ClientBadRequestException("잘못된 요청입니다.");
        val members = internalApiService.getMemberProfiles(filter, null, null, null, generation);
        val memberList = members.stream().map(m -> {
            val optionalActivity = m.getActivities().stream()
                    .filter(activity -> activity.getGeneration().equals(generation))
                    .filter(activity -> !organizerPartName.contains(activity.getPart()))
                    .findFirst();
            val part = optionalActivity.map(MemberSoptActivity::getPart).orElse(null);
            return memberMapper.toOfficialResponse(m, part, generation);
        }).collect(Collectors.toList());
        val generationMemberCount = internalApiService.getMemberCountByGeneration(generation);
        val response = new InternalAllOfficialMemberResponse(memberList, generationMemberCount);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @Operation(summary = "Auth token", description = "토큰 교환을 위한 엔드포인트")
    @PostMapping("/idp/auth/token")
    public ResponseEntity<InternalAuthResponse> exchangeAuthToken (
            @RequestHeader("x-api-key") String apiKey,
            @RequestHeader("x-request-from") String serviceName,
            @RequestBody InternalAuthRequest request
    ) {
        if (apiKey.equals(authConfig.getAppApiSecretKey()) && serviceName.equals("app")) {
            val authVo = internalApiService.authByToken(request.accessToken(), serviceName);
            val response = new InternalAuthResponse(authVo.accessToken(), authVo.errorCode());
            if (authVo.errorCode() != null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            else return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new InternalAuthResponse(null, "wrongApiKey"));
        }
    }
}
