package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.domain.Project;
import org.sopt.makers.internal.dto.member.MemberAllProfileResponse;
import org.sopt.makers.internal.dto.member.MemberProfileSpecificResponse;
import org.sopt.makers.internal.dto.member.MemberResponse;
import org.sopt.makers.internal.dto.project.ProjectDetailResponse;
import org.sopt.makers.internal.dto.project.ProjectLinkDao;
import org.sopt.makers.internal.dto.project.ProjectResponse;
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

    @Operation(summary = "Project id로 조회 API")
    @GetMapping("/projects/{id}")
    public ResponseEntity<ProjectDetailResponse> getProject (@PathVariable Long id) {
        val projectMembers = internalApiService.fetchById(id);
        val projectLinks = internalApiService.fetchLinksById(id);
        val response = projectMapper.toProjectDetailResponse(projectMembers, projectLinks);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Project 전체 조회 API")
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectResponse>> getProjects () {
        val projectMap = internalApiService.fetchAll().stream()
                .collect(Collectors.toMap(Project::getId, Function.identity()));
        val projectLinkMap = internalApiService.fetchAllLinks().stream()
                .collect(Collectors.groupingBy(ProjectLinkDao::id, Collectors.toList()));
        val projectIds = projectMap.keySet();
        val responses = projectIds.stream()
                .map(id -> projectMapper.toProjectResponse(projectMap.get(id), projectLinkMap.getOrDefault(id, List.of())))
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @Operation(summary = "자신의 토큰으로 조회 API")
    @GetMapping("/members/me")
    public ResponseEntity<MemberResponse> getMyInformation (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val member = internalApiService.getMemberById(memberDetails.getId());
        val response = memberMapper.toResponse(member);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "자신의 토큰으로 프로필 조회 API")
    @GetMapping("/members/profile/me")
    public ResponseEntity<MemberProfileSpecificResponse> getMyProfile (
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

    @Operation(
            summary = "멤버 프로필 전체 조회 API",
            description =
                    """
                    filter 1 -> 기획 / 2 -> 디자인 / 3 -> 웹 / 4 -> 서버 / 5 -> 안드로이드 / 6 -> iOS, 
                    참고로 asc(오름차순)로 정렬되어 있음
                    """
    )
    @GetMapping("/profile")
    public ResponseEntity<MemberAllProfileResponse> getUserProfiles (
            @RequestParam(required = false, name = "filter") Integer filter,
            @RequestParam(required = false, name = "limit") Integer limit,
            @RequestParam(required = false, name = "cursor") Integer cursor,
            @RequestParam(required = false, name = "name") String name,
            @RequestParam(required = false, name = "generation") Integer generation
    ) {
        val members = limit == null ? internalApiService.getMemberProfiles(filter, limit, cursor, name, generation) : internalApiService.getMemberProfiles(filter, limit + 1, cursor, name, generation);
        val memberList = members.stream().map(memberMapper::toProfileResponse).collect(Collectors.toList());
        val hasNextMember = (limit != null && memberList.size() > limit);
        if (hasNextMember) memberList.remove(members.size() - 1);
        val response = new MemberAllProfileResponse(memberList, hasNextMember);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
