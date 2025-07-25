package org.sopt.makers.internal.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.community.service.post.CommunityPostService;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.internal.dto.InternalLatestPostResponse;
import org.sopt.makers.internal.internal.dto.InternalMemberProjectResponse;
import org.sopt.makers.internal.internal.dto.InternalPopularPostResponse;
import org.sopt.makers.internal.internal.dto.InternalProjectResponse;
import org.sopt.makers.internal.project.domain.Project;
import org.sopt.makers.internal.project.dto.dao.ProjectLinkDao;
import org.sopt.makers.internal.project.dto.response.detailProject.ProjectDetailResponse;
import org.sopt.makers.internal.project.mapper.ProjectResponseMapper;
import org.sopt.makers.internal.project.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/api/v1")
@SecurityRequirement(name = "Authorization")
@Slf4j
@Tag(name = "내부 서비스 오픈 API")
public class InternalOpenApiController {

    private final InternalApiService internalApiService;
    private final PlatformService platformService;

    private final ProjectService projectService;
    private final ProjectResponseMapper projectMapper;

    private final CommunityPostService communityPostService;

    @Operation(summary = "Project id로 조회 API") // 공홈 사용
    @GetMapping("/projects/{id}")
    public ResponseEntity<ProjectDetailResponse> getProject (@PathVariable Long id) {
        ProjectDetailResponse response = projectService.getProjectDetailResponseById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Project 전체 조회 API") // 공홈 사용
    @GetMapping("/projects")
    public ResponseEntity<List<InternalProjectResponse>> getProjects () {
        Map<Long, Project> projectMap = internalApiService.fetchAll().stream()
                .collect(Collectors.toMap(Project::getId, Function.identity()));
        Map<Long, List<ProjectLinkDao>> projectLinkMap = internalApiService.fetchAllLinks().stream()
                .collect(Collectors.groupingBy(ProjectLinkDao::id, Collectors.toList()));
        val projectIds = projectMap.keySet();
        val responses = projectIds.stream()
                .map(id -> projectMapper.toInternalProjectResponse(projectMap.get(id), projectLinkMap.getOrDefault(id, List.of())))
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @Operation(summary = "회원 프로필 및 활동 정보 조회 API") // 앱팀 사용
    @GetMapping("members/{memberId}/project")
    public ResponseEntity<InternalMemberProjectResponse> getMemberProject(
            @PathVariable Long memberId
    ) {
        InternalUserDetails user = platformService.getInternalUser(memberId);
        int count = projectService.getProjectCountByMemberId(memberId);
        InternalMemberProjectResponse response = projectMapper.toInternalMemberProjectResponse(user, count);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "앱팀 Internal API 최신글 5개 조회", // 앱팀 사용
            description = "최상위 카테고리별(자유, 질문, 홍보, 파트Talk, 솝티클)로 최신글 1개씩 총 5개를 조회하는 API입니다.")
    @GetMapping("/community/posts/latest")
    public ResponseEntity<List<InternalLatestPostResponse>> getLatestPostsForApp() {
        List<InternalLatestPostResponse> response = communityPostService.getInternalLatestPosts();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "앱팀 Internal API 인기글 3 조회") // 앱팀 사용
    @GetMapping("/community/posts/popular")
    public ResponseEntity<List<InternalPopularPostResponse>> getPopularPosts() {
        int limit = 3;
        List<InternalPopularPostResponse> response = communityPostService.getPopularPostsForInternal(limit);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

//    @Operation(
//            summary = "최근 Community Post 조회 API",
//            description= """
//                    요청 category별 가장 최근의 게시물을 반환하는 API입니다. (default는 전체 중 최근 게시물을 반환)
//
//                    [대분류] 전체, 자유, 파트, SOPT 활동, 취업/진로, 홍보 \n
//                    * 각 대분류의 소분류로도 조회 가능합니다.
//            """)
//    @GetMapping("/community/post/recent")
//    public ResponseEntity<InternalCommunityPost> getRecentPostByCategory (
//            @RequestParam(required = false) String category
//    ) {
//        PostCategoryDao recentPost = communityPostService.getRecentPostByCategory(category);
//        InternalCommunityPost response = communityMapper.toInternalCommunityPostResponse(recentPost);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//    @Operation(summary = "자신의 토큰으로 조회 API")
//    @GetMapping("/members/me")
//    public ResponseEntity<InternalMemberResponse> getMyInformation (
//            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
//    ) {
//        val member = internalApiService.getMemberById(memberDetails.getId());
//        val latestGeneration = internalApiService.getMemberLatestActivityGeneration(memberDetails.getId());
//        val response = memberMapper.toInternalResponse(member, latestGeneration);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//
//    @Operation(summary = "자신의 토큰으로 활동한 활동정보 조회")
//    @GetMapping("/members/activity/me")
//    public ResponseEntity<InternalMemberActivityResponse> getMyActivityInformation (
//            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
//    ) {
//        val member = internalApiService.getMemberById(memberDetails.getId());
//        val memberActivity = internalApiService.getMemberActivities(memberDetails.getId());
//        val activityResponses = memberActivity.stream().map(activity ->
//                new InternalMemberActivityResponse.MemberSoptActivityResponse(
//                        activity.getId(), activity.getGeneration(), activity.getPart(), activity.getTeam())
//        ).collect(Collectors.toList());
//        val response = memberMapper.toInternalMemberActivityResponse(member, activityResponses);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//
//    @Operation(summary = "자신의 토큰으로 프로필 조회 API")
//    @GetMapping("/members/profile/me")
//    public ResponseEntity<InternalMemberProfileSpecificResponse> getMyProfile (
//            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
//    ) {
//        val id = memberDetails.getId();
//        val member = internalApiService.getMemberHasProfileById(id);
//        val memberProfileProjects = internalApiService.getMemberProfileProjects(id);
//        val activityMap = internalApiService.getMemberProfileActivity(
//                member.getActivities(),
//                memberProfileProjects
//        );
//        val activityResponses = activityMap.entrySet().stream().map(entry ->
//                new InternalMemberProfileSpecificResponse.MemberActivityResponse(entry.getKey(), entry.getValue())
//        ).collect(Collectors.toList());
//        val isMine = Objects.equals(member.getId(), memberDetails.getId());
//        val response = memberMapper.toInternalProfileSpecificResponse(
//                member, isMine, memberProfileProjects, activityResponses
//        );
//        sortProfileCareer(response);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//
//    @Operation(summary = "프로필 정보 기반 추천 친구 목록 조회 API",
//        description = """
//            key 필드는 유효한 추천 필터 값이 들어가야 함
//            - MBTI
//            - UNIVERSITY
//
//            *대소문자 무관
//            """)
//    @PostMapping("/members/profile/recommend")
//    public ResponseEntity<InternalRecommendMemberListResponse> getMyRecommendList (
//        @RequestBody InternalRecommendMemberListRequest request
//    ) {
//        val memberIds = internalApiService.getMembersIdByRecommendFilter(request.generations(),
//            request.getValueByKey(SearchContent.UNIVERSITY),
//            request.getValueByKey(SearchContent.MBTI));
//        val response = new InternalRecommendMemberListResponse(memberIds);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//
//    @Operation(
//            summary = "멤버 프로필 전체 조회 API",
//            description =
//                    """
//                    filter 1 -> 기획 / 2 -> 디자인 / 3 -> 웹 / 4 -> 서버 / 5 -> 안드로이드 / 6 -> iOS,
//                    참고로 asc(오름차순)로 정렬되어 있음
//                    """
//    )
//    @GetMapping("/profile")
//    public ResponseEntity<InternalMemberAllProfileResponse> getUserProfiles (
//            @RequestParam(required = false, name = "filter") Integer filter,
//            @RequestParam(required = false, name = "limit") Integer limit,
//            @RequestParam(required = false, name = "cursor") Integer cursor,
//            @RequestParam(required = false, name = "name") String name,
//            @RequestParam(required = false, name = "generation") Integer generation
//    ) {
//        val members = limit == null ? internalApiService.getMemberProfiles(filter, limit, cursor, name, generation) : internalApiService.getMemberProfiles(filter, limit + 1, cursor, name, generation);
//        val memberList = members.stream().map(memberMapper::toInternalProfileResponse).collect(Collectors.toList());
//        val hasNextMember = (limit != null && memberList.size() > limit);
//        if (hasNextMember) memberList.remove(members.size() - 1);
//        val response = new InternalMemberAllProfileResponse(memberList, hasNextMember);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//
//    @Operation(
//            summary = "공홈팀 멤버 프로필 조회 API",
//            description =
//                    """
//                    filter 1 -> 기획 / 2 -> 디자인 / 3 -> 웹 / 4 -> 서버 / 5 -> 안드로이드 / 6 -> iOS
//                    """
//    )
//    @GetMapping("/official/members/profile")
//    public ResponseEntity<InternalAllOfficialMemberResponse> getMemberProfilesByGenerationAndPart (
//            @RequestParam(required = false, name = "filter") Integer filter,
//            @RequestParam(name = "generation") Integer generation
//    ) {
//        if (generation == null) throw new ClientBadRequestException("잘못된 요청입니다.");
//        val members = internalApiService.getMemberProfiles(filter, null, null, null, generation);
//        val memberList = members.stream().map(m -> {
//            val generationActivities = m.getActivities().stream()
//                    .filter(activity -> activity.getGeneration().equals(generation)).toList();
//            String part;
//            if (generationActivities.size() > 1) {
//                val hasOnlyMemberRoleActivities = generationActivities.stream().filter(
//                        activity -> !organizerPartName.contains(activity.getPart())).toList();
//                if (hasOnlyMemberRoleActivities.isEmpty()) {
//                    part = generationActivities.get(0).getPart();
//                } else {
//                    part = hasOnlyMemberRoleActivities.get(0).getPart();
//                }
//            } else {
//                part = generationActivities.get(0).getPart();
//            }
//            return memberMapper.toOfficialResponse(m, part, generation);
//        }).collect(Collectors.toList());
//        val generationMemberCount = internalApiService.getMemberCountByGeneration(generation);
//        val response = new InternalAllOfficialMemberResponse(memberList, generationMemberCount);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//
//    @Operation(summary = "Auth token", description = "토큰 교환을 위한 엔드포인트")
//    @PostMapping("/idp/auth/token")
//    public ResponseEntity<InternalAuthResponse> exchangeAuthToken (
//            @RequestHeader("x-api-key") String apiKey,
//            @RequestHeader("x-request-from") String serviceName,
//            @RequestBody InternalAuthRequest request
//    ) {
//        if (apiKey.equals(authConfig.getAppApiSecretKey()) && serviceName.equals("app")) {
//            val authVo = internalApiService.authByToken(request.accessToken(), serviceName);
//            val response = new InternalAuthResponse(authVo.accessToken(), authVo.errorCode());
//            if (authVo.errorCode() != null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//            else return ResponseEntity.status(HttpStatus.OK).body(response);
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new InternalAuthResponse(null, "wrongApiKey"));
//        }
//    }
//
//    @Operation(summary = "최근 활동기수 멤버id 리스트 조회 API")
//    @GetMapping("/members/latest")
//    public ResponseEntity<InternalLatestMemberResponse> getUserProfiles (
//            @RequestParam(name = "generation") Integer generation
//    ) {
//        val memberIds = internalApiService.getMembersIdByGeneration(generation);
//        val response = new InternalLatestMemberResponse(memberIds);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//
//    @Operation(summary = "멤버 ID별 프로필 조회 API")
//    @GetMapping("/members/profile")
//    public ResponseEntity<List<InternalProfileListResponse>> getUserProfileList (
//            @RequestParam String memberIds
//    ) {
//        List<InternalProfileListResponse> responseArray = new ArrayList<>();
//        val members = memberService.getMemberProfileListById(memberIds);
//        for (Member member : members) {
//            val activities = memberService.getMemberProfileList(member.getActivities());
//            val activityResponses = activities.keySet().stream().map(MemberProfileSpecificResponse.MemberCardinalInfoResponse::new
//            ).toList();
//            responseArray.add(InternalProfileListResponse.builder()
//                    .memberId(member.getId())
//                    .profileImage(member.getProfileImage())
//                    .name(member.getName())
//                    .activities(activityResponses)
//                    .build()
//            );
//        }
//        return ResponseEntity.status(HttpStatus.OK).body(responseArray);
//    }
//
//    @Operation(summary = "명예 회원 멤버 id 리스트 조회 API")
//    @GetMapping("/members/inactivity")
//    public ResponseEntity<InternalInactivityMemberResponse> getInactivityMemberIdList(
//            @RequestParam(name = "generation") Integer generation,
//            @RequestParam(name = "part", required = false) Part part
//            ) {
//        val memberIds = internalApiService.getInactivityMemberIdListByGenerationAndPart(generation, part);
//        val response = new InternalInactivityMemberResponse(memberIds);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//
//    private void sortProfileCareer (InternalMemberProfileSpecificResponse response) {
//        response.careers().sort((a, b) -> {
//            val formatter = DateTimeFormatter.ofPattern("yyyy-MM");
//            val start = YearMonth.parse(a.startDate(), formatter);
//            val end = YearMonth.parse(b.startDate(), formatter);
//            return end.compareTo(start);
//        });
//        InternalMemberProfileSpecificResponse.MemberCareerResponse currentCareer = null;
//        int index = 0;
//        for (val career: response.careers()) {
//            if (career.isCurrent()) {
//                currentCareer = career;
//                break;
//            }
//            index += 1;
//        }
//        if (currentCareer != null) {
//            response.careers().add(0, currentCareer);
//            response.careers().remove(index+1);
//        }
//    }
//
//    @Operation(summary = "커피챗 오픈 유저 리스트 조회 API")
//    @GetMapping("/members/coffeechat")
//    public ResponseEntity<List<InternalCoffeeChatMemberResponse>> getCoffeeChatActivateMembers() {
//        List<InternalCoffeeChatMemberDto> members = memberService.getAllMemberByCoffeeChatActivate();
//        List<InternalCoffeeChatMemberResponse> response = coffeeChatResponseMapper.toInternalCoffeeChatMemberResponse(members);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
}
