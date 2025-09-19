package org.sopt.makers.internal.internal;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.sopt.makers.internal.community.service.post.CommunityPostService;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.internal.auth.ApiKeyValidator;
import org.sopt.makers.internal.internal.dto.CardinalInfoResponse;
import org.sopt.makers.internal.internal.dto.CreateDefaultUserProfileRequest;
import org.sopt.makers.internal.internal.dto.InternalLatestPostResponse;
import org.sopt.makers.internal.internal.dto.InternalMemberProfileListResponse;
import org.sopt.makers.internal.internal.dto.InternalMemberProfileResponse;
import org.sopt.makers.internal.internal.dto.InternalMemberProjectResponse;
import org.sopt.makers.internal.internal.dto.InternalPopularPostResponse;
import org.sopt.makers.internal.internal.dto.InternalRecommendMemberListRequest;
import org.sopt.makers.internal.internal.dto.InternalRecommendMemberListResponse;
import org.sopt.makers.internal.internal.dto.SearchContent;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.service.MemberService;
import org.sopt.makers.internal.project.dto.response.detailProject.ProjectDetailResponse;
import org.sopt.makers.internal.project.mapper.ProjectResponseMapper;
import org.sopt.makers.internal.project.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/api/v1")
@SecurityRequirement(name = "Authorization")
@Slf4j
@Tag(name = "내부 서비스 오픈 API")
public class InternalOpenApiController {

	private final InternalApiService internalApiService;
	private final PlatformService platformService;
	private final MemberService memberService;
	private final ProjectService projectService;
	private final CommunityPostService communityPostService;

	private final ProjectResponseMapper projectMapper;
	private final ApiKeyValidator apiKeyValidator;

	@Operation(summary = "Project id로 조회 - 공홈")
	@GetMapping("/projects/{id}")
	public ResponseEntity<ProjectDetailResponse> getProject(@PathVariable Long id) {
		ProjectDetailResponse response = projectService.getProjectDetailResponseById(id);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Operation(summary = "솝트에서 진행한 프로젝트 개수 조회 - 앱팀")
	@GetMapping("members/{memberId}/project")
	public ResponseEntity<InternalMemberProjectResponse> getMemberProject(
		@Parameter(description = "조회할 멤버 ID", example = "1") @PathVariable Long memberId) {
		int count = projectService.getProjectCountByMemberId(memberId);
		InternalMemberProjectResponse response = new InternalMemberProjectResponse(count);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Operation(summary = "커뮤니티 최신글 5개 조회 - 앱팀", description = "최상위 카테고리별(자유, 질문, 홍보, 파트Talk, 솝티클)로 최신글 1개씩 총 5개를 조회")
	@GetMapping("/community/posts/latest")
	public ResponseEntity<List<InternalLatestPostResponse>> getLatestPostsForApp() {
		List<InternalLatestPostResponse> response = communityPostService.getInternalLatestPosts();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Operation(summary = "커뮤니티 인기글 3 조회 - 앱팀")
	@GetMapping("/community/posts/popular")
	public ResponseEntity<List<InternalPopularPostResponse>> getPopularPosts() {
		int limit = 3;

		List<InternalPopularPostResponse> response = communityPostService.getPopularPostsForInternal(limit);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Operation(summary = "단일 멤버 ID별 상세 프로필 조회 - 앱팀")
	@GetMapping("/members/profile/me")
	public ResponseEntity<InternalMemberProfileResponse> getUserProfile(
		@Parameter(description = "조회할 멤버 ID", example = "1") @RequestParam String memberId) {
		Member member = memberService.getMemberById(Long.valueOf(memberId));
		InternalUserDetails userDetails = platformService.getInternalUser(Long.valueOf(memberId));

		List<CardinalInfoResponse> activityResponses = userDetails.soptActivities()
			.stream()
			.map(activity -> new CardinalInfoResponse(activity.generation() + "," + activity.part()))
			.toList();

		InternalMemberProfileResponse response = InternalMemberProfileResponse.builder()
			.memberId(member.getId())
			.profileImage(userDetails.profileImage())
			.name(userDetails.name())
			.mbti(member.getMbti())
			.introduction(member.getIntroduction())
			.university(member.getUniversity())
			.activities(activityResponses)
			.build();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Operation(summary = "다중 멤버 ID별 프로필 조회 - 앱팀")
	@GetMapping("/members/profile")
	public ResponseEntity<List<InternalMemberProfileListResponse>> getUserProfileList(
		@Parameter(description = "조회할 멤버 ID 목록", example = "1,2") @RequestParam String memberIds) {
		List<InternalMemberProfileListResponse> responseArray = new ArrayList<>();

		List<Long> userIds = Arrays.stream(URLDecoder.decode(memberIds, StandardCharsets.UTF_8).split(","))
			.filter(s -> !s.isBlank())
			.map(Long::valueOf)
			.toList();
		List<Member> members = memberService.getMemberProfileListById(userIds);
		List<InternalUserDetails> internalMembers = platformService.getInternalUsers(userIds);
		Map<Long, InternalUserDetails> internalUserDetailsMap = internalMembers.stream()
			.collect(Collectors.toMap(InternalUserDetails::userId, Function.identity(),
				(existing, replacement) -> existing));

		for (Member member : members) {
			InternalUserDetails userDetails = internalUserDetailsMap.get(member.getId());
			List<CardinalInfoResponse> activityResponses = userDetails.soptActivities()
				.stream()
				.map(activity -> new CardinalInfoResponse(activity.generation() + "," + activity.part()))
				.toList();

			responseArray.add(InternalMemberProfileListResponse.builder()
				.memberId(member.getId())
				.profileImage(userDetails.profileImage())
				.name(userDetails.name())
				.introduction(member.getIntroduction())
				.activities(activityResponses)
				.build());
		}
		return ResponseEntity.status(HttpStatus.OK).body(responseArray);
	}

	@Operation(summary = "프로필 정보 기반 추천 친구 목록 조회 API - 앱팀", description = """
		key 필드는 유효한 추천 필터 값이 들어가야 함
		- MBTI
		- UNIVERSITY
		""")
	@PostMapping("/members/profile/recommend")
	public ResponseEntity<InternalRecommendMemberListResponse> getMyRecommendList(@RequestBody @Valid InternalRecommendMemberListRequest request) {
		Set<Long> memberIds = internalApiService.getMemberIdsByRecommendFilter(
				request.generations(),
				request.getValueByKey(SearchContent.UNIVERSITY),
				request.getValueByKey(SearchContent.MBTI)
		);
		InternalRecommendMemberListResponse response = new InternalRecommendMemberListResponse(memberIds);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "기본 유저 프로필 생성 - 플랫폼팀")
	@PostMapping("/members")
	public ResponseEntity<String> createUserProfile(
		@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "플그 기본 유저 프로필 생성 요청", required = true, content = @Content(schema = @Schema(implementation = CreateDefaultUserProfileRequest.class))) @RequestBody CreateDefaultUserProfileRequest request,
		@RequestHeader("apiKey") String apiKey) {
		apiKeyValidator.validate(apiKey);

		Member member = memberService.saveDefaultMemberProfile(request.userId());
		return ResponseEntity.status(HttpStatus.CREATED).body("기본 유저 프로필이 성공적으로 생성되었습니다. user id: " + member.getId());
	}

	@Operation(summary = "기본 유저 프로필 삭제 - 플랫폼팀")
	@DeleteMapping("/members/{memberId}")
	public ResponseEntity<String> deleteUserProfile(@PathVariable Long memberId,
		@RequestHeader("apiKey") String apiKey) {
		apiKeyValidator.validate(apiKey);

		memberService.deleteDefaultMemberProfile(memberId);
		return ResponseEntity.status(HttpStatus.OK).body("기본 유저 프로필이 성공적으로 삭제되었습니다. user id: " + memberId);
	}
}
