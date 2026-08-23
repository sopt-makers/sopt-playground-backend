package org.sopt.makers.internal.member.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.coffeechat.service.CoffeeChatRetriever;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.enums.OrderByCondition;
import org.sopt.makers.internal.member.dto.profile.MemberProfileSummaryVo;
import org.sopt.makers.internal.member.dto.response.MemberAllProfileResponse;
import org.sopt.makers.internal.member.dto.response.MemberProfileResponse;
import org.sopt.makers.internal.member.mapper.MemberMapper;
import org.sopt.makers.internal.member.mapper.MemberResponseMapper;
import org.sopt.makers.internal.member.repository.MemberProfileQueryRepository;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.sopt.makers.internal.member.service.sorting.MemberSortingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 멤버 프로필 목록 조회(GET /api/v1/members/profile) 전용 서비스.
 *
 * <p>{@code MemberService} 에서 분리했다. 이 조회는 다른 멤버 기능과 공유하는 상태가 없고,
 * 전용 헬퍼 5개와 {@link MemberSortingService} 이하 정렬 기계장치(전략 3 + 비교자 5)를
 * 통째로 자기 것으로 가진다.
 *
 * <p>조회 흐름:
 * <ol>
 *   <li>DB 필터(mbti, employed)로 대상 ID 를 모두 조회</li>
 *   <li>플랫폼 서버에서 해당 유저들의 기본 정보를 가져와 part/team/generation 으로 걸러냄</li>
 *   <li>정렬·검색용 Projection 조회 (엔티티를 로드하지 않는다)</li>
 *   <li>검색어 필터 → 정렬 → 페이징</li>
 *   <li>페이지 대상(기본 30건)에 대해서만 엔티티를 fetch join 으로 로드해 응답 매핑</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberProfileListService {

	private static final int QUESTION_PREVIEW_DAYS = 7;

	private final MemberRepository memberRepository;
	private final MemberProfileQueryRepository memberProfileQueryRepository;
	private final MemberQuestionRetriever memberQuestionRetriever;
	private final CoffeeChatRetriever coffeeChatRetriever;
	private final PlatformService platformService;
	private final MemberSortingService memberSortingService;
	private final MemberMapper memberMapper;
	private final MemberResponseMapper memberResponseMapper;

	@Transactional(readOnly = true)
	public MemberAllProfileResponse getMemberProfiles(Integer filter, Integer limit, Integer offset, String search,
		Integer generation, Integer employed, Integer orderBy, String mbti, String team) {
		// 1) DB에서 먼저 서버 필터(mbti, employed)로 해당하는 모든 userId 조회
		List<Long> allFilteredIds = memberProfileQueryRepository.findAllMemberIdsByDbFilters(mbti, employed, search);
		if (allFilteredIds.isEmpty()) {
			return new MemberAllProfileResponse(Collections.emptyList(), false, 0);
		}

		// 2) part/team/generation, name 정렬/검색은 플랫폼 데이터로 보정 필요 → 해당 ID 리스트로 플랫폼 조회
		List<InternalUserDetails> internalUsers = platformService.getInternalUsers(allFilteredIds);
		
		// part/team/generation, name 필터 적용
		String part = getMemberPart(filter);
		String checkedTeam = checkActivityTeamConditions(team);
		List<InternalUserDetails> filteredByPlatform = internalUsers.stream()
			.filter(u -> filterPlatformConditions(u, part, checkedTeam, generation))
			.toList();

		if (filteredByPlatform.isEmpty()) {
			return new MemberAllProfileResponse(Collections.emptyList(), false, 0);
		}

		// 3) 정렬·검색에 필요한 값만 Projection 으로 조회한다.
		//    엔티티를 로드하지 않으므로 가중치 계산 중 LAZY 컬렉션(links/careers) 접근이 발생하지 않는다.
		Map<Long, MemberProfileSummaryVo> memberProfileSummaryMap = memberProfileQueryRepository.findMemberProfileSummariesByIds(
			filteredByPlatform.stream().map(InternalUserDetails::userId).toList()
		).stream().collect(Collectors.toMap(MemberProfileSummaryVo::id, Function.identity()));

		// 검색어가 이름/대학교/회사 모두에 적용되도록 추가 필터링 (토큰 AND, 필드 OR)
		List<InternalUserDetails> filteredBySearch = filteredByPlatform.stream()
			.filter(u -> matchesSearchAcrossFields(u, memberProfileSummaryMap.get(u.userId()), search))
			.toList();

		if (filteredBySearch.isEmpty()) {
			return new MemberAllProfileResponse(Collections.emptyList(), false, 0);
		}

		// 3-1) Member 정보를 포함한 정렬 및 페이지네이션 처리
		int offsetValue = (offset == null || offset < 0) ? 0 : offset;
		int limitValue = (limit == null || limit <= 0) ? 30 : limit;

		// orderBy 파라미터가 있으면 orderBy 우선, 없으면 필터별 정렬 정책 적용
		List<InternalUserDetails> sortedUsers;
		if (orderBy != null) {
			// orderBy 파라미터가 있을 때: OrderByCondition 기준으로 정렬
			OrderByCondition orderByCondition = OrderByCondition.valueOf(orderBy);
			sortedUsers = filteredBySearch.stream()
				.sorted(memberSortingService.createComparatorByOrderCondition(memberProfileSummaryMap, orderByCondition, employed))
				.toList();
		} else {
			// orderBy가 없을 때: 필터별 정렬 정책 적용
			sortedUsers = filteredBySearch.stream()
				.sorted(memberSortingService.createComparator(memberProfileSummaryMap, employed, checkedTeam))
				.toList();
		}
		
		List<InternalUserDetails> pagedByServer = sortedUsers.stream()
			.skip(offsetValue)
			.limit(limitValue)
			.toList();

		if (pagedByServer.isEmpty()) {
			return new MemberAllProfileResponse(Collections.emptyList(), false, 0);
		}
		List<Long> pagedMemberIds = pagedByServer.stream()
			.map(InternalUserDetails::userId)
			.toList();

		// 응답 매핑에는 Member 엔티티가 필요하지만, 페이지 대상(기본 30건)에 대해서만 로드한다.
		// links/careers 는 둘 다 List(bag) 이라 한 쿼리에서 동시에 fetch join 할 수 없어(MultipleBagFetchException)
		// 두 번에 나눈다. 같은 트랜잭션이므로 두 번째 쿼리가 동일한 영속 인스턴스에 links 를 채운다.
		List<Member> pagedMembers = memberRepository.findAllByIdInWithCareers(pagedMemberIds);
		memberRepository.findAllByIdInWithLinks(pagedMemberIds);
		Map<Long, Member> pagedMemberMap = pagedMembers.stream()
			.collect(Collectors.toMap(Member::getId, Function.identity()));

		Map<Long, MemberProfileResponse.MemberQuestionPreviewResponse> questionPreviewByReceiverId =
			memberQuestionRetriever.findLatestRecentQuestionsByReceiverIds(
				pagedMemberIds,
				LocalDateTime.now().minusDays(QUESTION_PREVIEW_DAYS)
			).stream().collect(Collectors.toMap(
				question -> question.getReceiver().getId(),
				question -> new MemberProfileResponse.MemberQuestionPreviewResponse(
					question.getId(),
					question.getContent()
				)
			));

		List<MemberProfileResponse> memberList = pagedByServer.stream()
			.map(userDetails -> {
				Member member = pagedMemberMap.get(userDetails.userId());
				boolean isCoffeeChatActivate = member != null && coffeeChatRetriever.existsCoffeeChat(member);

				MemberProfileResponse baseResponse = memberMapper.toProfileResponse(
					member,
					userDetails,
					isCoffeeChatActivate
				);

				MemberProfileResponse.MemberQuestionPreviewResponse questionPreview =
					questionPreviewByReceiverId.get(userDetails.userId());

				return memberResponseMapper.attachQuestionPreview(baseResponse, questionPreview);
			})
			.toList();

		// 4) hasNext 및 totalCount 계산 (서버 기준)
		boolean hasNext = (offsetValue + limitValue) < sortedUsers.size();
		int totalCount = sortedUsers.size();

		return new MemberAllProfileResponse(memberList, hasNext, totalCount);
	}

	private boolean filterPlatformConditions(
		InternalUserDetails userDetails,
		String part,
		String team,
		Integer generation
	) {
		if (part == null && team == null && generation == null) {
			return true;
		}

		List<SoptActivity> activities = userDetails.soptActivities();

		return activities.stream().anyMatch(activity -> {
			// 공통 조건: generation과 part 체크
			boolean generationMatch = (generation == null || Objects.equals(activity.generation(), generation));
			boolean partMatch = (
				part == null ||
					Objects.equals(normalizeMemberTabPartFilterActivityPart(activity.part()), part)
			);

			if (!generationMatch || !partMatch) {
				return false;
			}

			if (team == null) {
				return true;
			}

			// 팀 조건 체크
			if ("임원진".equals(team)) {
				// 임원진: 솝트 활동인 동시에 미디어팀, 운영팀이 아닌 다른 팀이 있는 경우
				String activityTeam = activity.team();
				return activityTeam != null
					&& activity.isSopt()
					&& !activityTeam.isEmpty()
					&& !"미디어팀".equals(activityTeam)
					&& !"운영팀".equals(activityTeam);
			}

			if ("메이커스".equals(team)) {
				return !activity.isSopt() || Objects.equals(activity.team(), "메이커스");
			}

			return Objects.equals(activity.team(), team);
		});
	}

	/**
	 * 검색어 기반 필터링
	 */
	private boolean matchesSearchAcrossFields(InternalUserDetails userDetails, MemberProfileSummaryVo member, String search) {
		if (search == null || search.isBlank()) {
			return true;
		}
		String keyword = search.trim();
		String name = userDetails != null ? userDetails.name() : null;

		boolean inName = name != null && name.contains(keyword);
		boolean inUniversityOrCompany = member != null && member.matchesUniversityOrCompany(keyword);

		return inName || inUniversityOrCompany;
	}

	private String normalizeMemberTabPartFilterActivityPart(String activityPart) {
		if (activityPart == null || activityPart.isBlank()) {
			return null;
		}

		return switch (activityPart) {
			case "기획", "PLAN", "PM" -> "PLAN";
			case "디자인", "DESIGN" -> "DESIGN";
			case "웹", "WEB", "FRONTEND", "프론트엔드" -> "WEB";
			case "서버", "SERVER", "BACKEND", "백엔드" -> "SERVER";
			case "안드로이드", "ANDROID" -> "ANDROID";
			case "iOS", "IOS" -> "IOS";
			default -> activityPart;
		};
	}

	private String getMemberPart(Integer filter) {
		if (filter == null) {
			return null;
		}

		return switch (filter) {
			case 1 -> "PLAN";
			case 2 -> "DESIGN";
			case 3 -> "WEB";
			case 4 -> "SERVER";
			case 5 -> "ANDROID";
			case 6 -> "IOS";
			default -> null;
		};
	}

	private String checkActivityTeamConditions(String team) {
		if (team == null || team.equals("해당 없음")) {
			return null;
		}

		if (team.equals("MAKERS")) {
			return "메이커스";
		}
		if (team.equals("OPERATION")) {
			return "운영팀";
		}
		if (team.equals("MEDIA")) {
			return "미디어팀";
		}
		if (team.equals("EXECUTIVE")) {
			return "임원진";
		}

		return null;
	}
}
