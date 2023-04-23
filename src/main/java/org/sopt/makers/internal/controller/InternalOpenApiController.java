package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.config.AuthConfig;
import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.domain.Project;
import org.sopt.makers.internal.dto.internal.*;
import org.sopt.makers.internal.dto.project.ProjectLinkDao;
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
@Tag(name = "내부 서비스 오픈 API")
public class InternalOpenApiController {

    private final InternalApiService internalApiService;
    private final ProjectResponseMapper projectMapper;
    private final MemberMapper memberMapper;

    private final AuthConfig authConfig;

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
                    filter : 
                        1 -> 기획 / 2 -> 디자인 / 3 -> 웹 / 4 -> 서버 / 5 -> 안드로이드 / 6 -> iOS, 
                        참고로 asc(오름차순)로 정렬되어 있음 \n
                    sojuCapacity :  
                        0 -> 못마셔요 / 0.5 -> 0.5병 / 1.0 -> 1병 / 1.5 -> 1.5병 /
                        2.0 -> 2병 / 2.5 -> 2.5병 / 3.0 -> 3병 이상 \n
                    orderByDropDown : 
                        1 -> 최근에 등록했순 / 2 -> 예전에 등록했순 / 3 -> 최근에 활동했순 / 4 -> 예전에 활동했순 \n
                    team : 임원진, 운영팀, 미디어팀, 메이커스
                    """
    )
    @GetMapping("/profile")
    public ResponseEntity<InternalMemberAllProfileResponse> getUserProfiles (
            @RequestParam(required = false, name = "filter") Integer filter,
            @RequestParam(required = false, name = "limit") Integer limit,
            @RequestParam(required = false, name = "cursor") Integer cursor,
            @RequestParam(required = false, name = "name") String name,
            @RequestParam(required = false, name = "generation") Integer generation,
            @RequestParam(required = false, name = "sojuCapactiy") Double sojuCapactiy,
            @RequestParam(required = false, name = "orderByDropDown") Integer orderByDropDown,
            @RequestParam(required = false, name = "mbti") String mbti,
            @RequestParam(required = false, name = "team") String team
    ) {
        val members = limit == null ?
            internalApiService.getMemberProfiles(filter, limit, cursor, name, generation, sojuCapactiy, orderByDropDown, mbti, team) :
            internalApiService.getMemberProfiles(filter, limit + 1, cursor, name, generation, sojuCapactiy, orderByDropDown, mbti, team);
        val memberList = members.stream().map(memberMapper::toInternalProfileResponse).collect(Collectors.toList());
        val hasNextMember = (limit != null && memberList.size() > limit);
        if (hasNextMember) memberList.remove(members.size() - 1);
        val response = new InternalMemberAllProfileResponse(memberList, hasNextMember);
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
