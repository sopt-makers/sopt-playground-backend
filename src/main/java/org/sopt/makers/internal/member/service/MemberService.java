package org.sopt.makers.internal.member.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sopt.makers.internal.coffeechat.dto.request.MemberCoffeeChatPropertyDto;
import org.sopt.makers.internal.coffeechat.service.CoffeeChatRetriever;
import org.sopt.makers.internal.common.Constant;
import org.sopt.makers.internal.community.repository.post.CommunityPostRepository;
import org.sopt.makers.internal.community.service.ReviewService;
import org.sopt.makers.internal.exception.BadRequestException;
import org.sopt.makers.internal.exception.ConflictException;
import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.platform.PlatformUserUpdateRequest;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.external.platform.UserSearchResponse;
import org.sopt.makers.internal.external.slack.SlackClient;
import org.sopt.makers.internal.external.slack.SlackMessageUtil;
import org.sopt.makers.internal.member.constants.AppJamObMemberId;
import org.sopt.makers.internal.member.constants.AskMemberId;
import org.sopt.makers.internal.member.constants.MakersMemberId;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberBlock;
import org.sopt.makers.internal.member.domain.MemberCareer;
import org.sopt.makers.internal.member.domain.MemberLink;
import org.sopt.makers.internal.member.domain.MemberReport;
import org.sopt.makers.internal.member.domain.TlMember;
import org.sopt.makers.internal.member.domain.UserFavor;
import org.sopt.makers.internal.member.domain.WorkPreference;
import org.sopt.makers.internal.member.domain.enums.ActivityTeam;
import org.sopt.makers.internal.member.domain.enums.OrderByCondition;
import org.sopt.makers.internal.member.domain.enums.Part;
import org.sopt.makers.internal.member.domain.enums.ServiceType;
import org.sopt.makers.internal.member.dto.ActivityVo;
import org.sopt.makers.internal.member.dto.MemberProfileProjectDao;
import org.sopt.makers.internal.member.dto.MemberProfileProjectVo;
import org.sopt.makers.internal.member.dto.request.MemberProfileSaveRequest;
import org.sopt.makers.internal.member.dto.request.MemberProfileUpdateRequest;
import org.sopt.makers.internal.member.dto.request.WorkPreferenceUpdateRequest;
import org.sopt.makers.internal.member.dto.response.MakersMemberProfileResponse;
import org.sopt.makers.internal.member.dto.response.MemberAllProfileResponse;
import org.sopt.makers.internal.member.dto.response.MemberBlockResponse;
import org.sopt.makers.internal.member.dto.response.MemberCareerResponse;
import org.sopt.makers.internal.member.dto.response.MemberInfoResponse;
import org.sopt.makers.internal.member.dto.response.MemberProfileResponse;
import org.sopt.makers.internal.member.dto.response.MemberProfileSpecificResponse;
import org.sopt.makers.internal.member.dto.response.MemberProfileSpecificResponse.MemberActivityResponse;
import org.sopt.makers.internal.member.dto.response.MemberPropertiesResponse;
import org.sopt.makers.internal.member.dto.response.MemberResponse;
import org.sopt.makers.internal.member.dto.response.MemberSoptActivityResponse;
import org.sopt.makers.internal.member.dto.response.AskMemberResponse;
import org.sopt.makers.internal.member.dto.response.WorkPreferenceRecommendationResponse;
import org.sopt.makers.internal.member.dto.response.WorkPreferenceResponse;
import org.sopt.makers.internal.member.dto.response.TlMemberResponse;
import org.sopt.makers.internal.member.mapper.MemberMapper;
import org.sopt.makers.internal.member.mapper.MemberResponseMapper;
import org.sopt.makers.internal.member.repository.MemberBlockRepository;
import org.sopt.makers.internal.member.repository.MemberLinkRepository;
import org.sopt.makers.internal.member.repository.MemberProfileQueryRepository;
import org.sopt.makers.internal.member.repository.MemberReportRepository;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.sopt.makers.internal.member.repository.career.MemberCareerRepository;
import org.sopt.makers.internal.member.service.career.MemberCareerRetriever;
import org.sopt.makers.internal.member.service.sorting.MemberSortingService;
import org.sopt.makers.internal.member.service.workpreference.WorkPreferenceRetriever;
import org.sopt.makers.internal.member.service.workpreference.WorkPreferenceModifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {
	private final MemberRetriever memberRetriever;
	private final TlMemberRetriever tlMemberRetriever;
	private final CoffeeChatRetriever coffeeChatRetriever;
	private final MemberCareerRetriever memberCareerRetriever;
	private final MemberResponseMapper memberResponseMapper;
	private final MemberRepository memberRepository;
	private final MemberLinkRepository memberLinkRepository;
	private final CommunityPostRepository communityPostRepository;
	private final MemberCareerRepository memberCareerRepository;
	private final MemberProfileQueryRepository memberProfileQueryRepository;
	private final MemberReportRepository memberReportRepository;
	private final MemberBlockRepository memberBlockRepository;
	private final MemberMapper memberMapper;
	private final SlackClient slackClient;
	private final SlackMessageUtil slackMessageUtil;
	private final ReviewService reviewService;
	private final PlatformService platformService;
	private final MemberSortingService memberSortingService;
	private final WorkPreferenceRetriever workPreferenceRetriever;
	private final WorkPreferenceModifier workPreferenceModifier;
	private final AskMemberId askMemberId;
	private final MemberQuestionRetriever memberQuestionRetriever;

	private static final int QUESTION_PREVIEW_DAYS = 7;
	private static final int RECENT_QUESTION_DAYS = 7;

	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Transactional(readOnly = true)
	public MemberInfoResponse getMyInformation(Long userId) {
		Member member = getMemberById(userId);
		List<Long> appJamObMemberIds = AppJamObMemberId.getAppJamObMember();
		InternalUserDetails userDetails = platformService.getInternalUser(userId);
		boolean isCoffeeChatActive = coffeeChatRetriever.existsCoffeeChat(member);
		boolean enableWorkPreferenceEvent =
			Objects.equals(userDetails.lastGeneration(), Constant.CURRENT_GENERATION) || appJamObMemberIds.contains(userId);

		return memberResponseMapper.toMemberInfoResponse(member, userDetails, isCoffeeChatActive, enableWorkPreferenceEvent);
	}

	@Transactional(readOnly = true)
	public List<MemberResponse> getMemberByName(String name) {
		val orderBy = OrderByCondition.LATEST_GENERATION.name();
		UserSearchResponse searchResponse = platformService.searchInternalUsers(null, null, null, name, 30, 0, orderBy);

		List<InternalUserDetails> userDetailsList = searchResponse.profiles();
		if (userDetailsList.isEmpty()) {
			return Collections.emptyList();
		}

		List<Long> userIds = userDetailsList.stream().map(InternalUserDetails::userId).toList();

		Map<Long, Member> memberMap = memberRepository.findAllByIdIn(userIds)
			.stream()
			.collect(Collectors.toMap(Member::getId, Function.identity()));

		return userDetailsList.stream().map(userDetails -> {
			Member member = memberMap.get(userDetails.userId());

			boolean hasProfile = (member != null) && member.getHasProfile();
			boolean editActivitiesAble = (member != null) ? member.getEditActivitiesAble() : true;

			return new MemberResponse(userDetails.userId(), userDetails.name(), userDetails.lastGeneration(),
				userDetails.profileImage(), hasProfile, editActivitiesAble);
		}).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public MemberProfileSpecificResponse getMemberProfile(Long profileId, Long viewerId) {
		Member member = getMemberHasProfileById(profileId);
		boolean isMine = Objects.equals(profileId, viewerId);
		InternalUserDetails userDetails = platformService.getInternalUser(profileId);
		List<MemberProfileProjectDao> memberProfileProjects = getMemberProfileProjects(profileId);
		val activityMap = getMemberProfileActivity(userDetails.soptActivities(), memberProfileProjects);
		val soptActivity = getMemberProfileProjects(userDetails.soptActivities(), memberProfileProjects);
		List<MemberProfileProjectVo> soptActivityResponse = soptActivity.stream()
			.map(m -> new MemberProfileProjectVo(m.id(), m.generation(), m.part(), checkTeamNullCondition(m.team()),
				m.projects()))
			.toList();
		List<MemberActivityResponse> activityResponses = activityMap.entrySet()
			.stream()
			.map(entry -> new MemberActivityResponse(entry.getKey(), entry.getValue()))
			.collect(Collectors.toList());

		boolean isCoffeeChatActivate = coffeeChatRetriever.existsCoffeeChat(member);
		boolean hasRecentQuestion = memberQuestionRetriever.existsRecentQuestionByReceiver(
			profileId,
			LocalDateTime.now().minusDays(RECENT_QUESTION_DAYS)
		);

		MemberProfileSpecificResponse response = memberMapper.toProfileSpecificResponse(member, userDetails, isMine,
			memberProfileProjects, activityResponses, isCoffeeChatActivate);

		// 정렬된 soptActivityResponse에서 직접 SoptMemberActivityResponse 생성
		List<MemberProfileSpecificResponse.SoptMemberActivityResponse> updatedActivities = soptActivityResponse.stream()
			.map(vo -> new MemberProfileSpecificResponse.SoptMemberActivityResponse(
				vo.generation(), vo.part(), vo.team(), vo.projects()))
			.toList();

		// updatedActivities set 해주기
		MemberProfileSpecificResponse updateResponse = new MemberProfileSpecificResponse(response.name(),
			response.profileImage(), response.birthday(), response.isPhoneBlind(), response.phone(), response.email(),
			response.address(), response.university(), response.major(), response.introduction(), response.skill(),
			response.mbti(), response.mbtiDescription(), response.sojuCapacity(), response.interest(),
			response.userFavor(), response.idealType(), response.selfIntroduction(), response.workPreference(), response.activities(),
			updatedActivities, response.links(), response.projects(), response.careers(), response.allowOfficial(),
			hasRecentQuestion, response.isCoffeeChatActivate(), response.isMine());
		return MemberProfileSpecificResponse.applyPhoneMasking(updateResponse, isMine, isCoffeeChatActivate);
	}

	@Transactional(readOnly = true)
	public Member getMemberById(Long id) {
		return memberRetriever.findMemberById(id);
	}

	public MemberResponse getMemberResponseById(Long id) {
		InternalUserDetails user = platformService.getInternalUser(id);
		Member member = getMemberById(id);

		return new MemberResponse(id, user.name(), user.lastGeneration(), user.profileImage(), member.getHasProfile(),
			member.getEditActivitiesAble());
	}

	@Transactional(readOnly = true)
	public Member getMemberHasProfileById(Long id) {
		Member member = memberRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("해당 id의 Member를 찾을 수 없습니다."));
		if (member.getHasProfile())
			return member;
		else
			throw new BadRequestException("해당 Member는 프로필이 없습니다.");
	}

	@Transactional(readOnly = true)
	public List<Member> getMemberProfileListById(List<Long> idList) {
		List<Member> members = new ArrayList<>();

		for (Long id : idList) {
			Member member = memberRepository.findById(id).orElse(null);

			if (member != null && member.getHasProfile()) {
				members.add(member);
			}
		}

		return members;
	}

	@Transactional(readOnly = true)
	public List<MemberProfileProjectDao> getMemberProfileProjects(Long id) {
		return memberProfileQueryRepository.findMemberProfileProjectsByMemberId(id);
	}

	public Map<String, List<ActivityVo>> getMemberProfileActivity(List<SoptActivity> soptActivities,
		List<MemberProfileProjectDao> memberProfileProjects) {
		if (soptActivities.isEmpty()) {
			throw new NotFoundException("30기 이전 기수 활동 회원은 공식 채널로 문의해주시기 바랍니다.");
		}

		// 같은 generation에 대한 중복 키 처리 - SOPT(isSopt=true) 우선
		val sortedActivities = soptActivities.stream()
			.sorted(Comparator.comparing(SoptActivity::normalizedGeneration)
				.thenComparing(activity -> !activity.isSopt()))
			.toList();
		val cardinalInfoMap = sortedActivities.stream()
			.collect(Collectors.toMap(SoptActivity::generation, SoptActivity::part, (p1, p2) -> p1));

		val activities = sortedActivities.stream().map(a -> memberMapper.toActivityInfoVo(a, false));

		val projects = memberProfileProjects.stream().filter(p -> p.generation() != null).map(p -> {
			val part = cardinalInfoMap.getOrDefault(p.generation(), "");
			return memberMapper.toActivityInfoVo(p, true, part);
		});

		val genActivityMap = Stream.concat(activities, projects).collect(Collectors.groupingBy(ActivityVo::generation));

		// 정렬된 기수 순서를 유지하기 위해 LinkedHashMap 사용
		Map<String, List<ActivityVo>> result = new java.util.LinkedHashMap<>();
		sortedActivities.stream()
			.map(SoptActivity::generation)
			.distinct()
			.forEach(gen -> {
				List<ActivityVo> genActivities = genActivityMap.get(gen);
				if (genActivities != null) {
					result.put(gen + "," + cardinalInfoMap.getOrDefault(gen, ""), genActivities);
				}
			});
		return result;
	}

	public List<MemberProfileProjectVo> getMemberProfileProjects(List<SoptActivity> soptActivities,
		List<MemberProfileProjectDao> memberProfileProjects) {
		// 모든 활동 (SOPT + 메이커스)을 포함, 같은 기수에서 SOPT(isSopt=true) 우선
		return soptActivities.stream()
			.sorted(Comparator.comparing(SoptActivity::normalizedGeneration)
				.thenComparing(activity -> !activity.isSopt()))
			.map(m -> {
				val projects = memberProfileProjects.stream()
					.filter(p -> p.generation() != null)
					.filter(p -> p.generation().equals(m.generation()))
					.map(memberMapper::toActivityInfoVo)
					.collect(Collectors.toList());
				return memberMapper.toSoptMemberProfileProjectVo(m, projects);
			}).collect(Collectors.toList());
	}

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

		// 3) DB 멤버 로드
		Map<Long, Member> memberMap = memberRepository.findAllByIdIn(
			filteredByPlatform.stream().map(InternalUserDetails::userId).toList()
		).stream().collect(Collectors.toMap(Member::getId, Function.identity()));

		// 검색어가 이름/대학교/회사 모두에 적용되도록 추가 필터링 (토큰 AND, 필드 OR)
		List<InternalUserDetails> filteredBySearch = filteredByPlatform.stream()
			.filter(u -> matchesSearchAcrossFields(u, memberMap.get(u.userId()), search))
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
				.sorted(memberSortingService.createComparatorByOrderCondition(memberMap, orderByCondition, employed))
				.toList();
		} else {
			// orderBy가 없을 때: 필터별 정렬 정책 적용
			sortedUsers = filteredBySearch.stream()
				.sorted(memberSortingService.createComparator(memberMap, employed, checkedTeam))
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
				Member member = memberMap.get(userDetails.userId());
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
	private boolean matchesSearchAcrossFields(InternalUserDetails userDetails, Member member, String search) {
		if (search == null || search.isBlank()) {
			return true;
		}
		String keyword = search.trim();
		String name = userDetails != null ? userDetails.name() : null;
		String university = (member != null && member.getUniversity() != null) ? member.getUniversity() : null;
		List<MemberCareer> careers = (member != null && member.getCareers() != null) ? member.getCareers() : Collections.emptyList();

		boolean inName = name != null && name.contains(keyword);
		boolean inUniv = university != null && university.contains(keyword);
		boolean inCompany = careers.stream()
			.map(MemberCareer::getCompanyName)
			.filter(Objects::nonNull)
			.anyMatch(c -> c.contains(keyword));

		return inName || inUniv || inCompany;
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

	public Member saveDefaultMemberProfile(Long userId) {
		if (memberRepository.existsById(userId)) {
			throw new ConflictException("이미 존재하는 유저입니다. userId=" + userId);
		}

		Member member = Member.builder()
			.id(userId)
			.address(null)
			.university(null)
			.major(null)
			.introduction(null)
			.mbti(null)
			.mbtiDescription(null)
			.sojuCapacity(null)
			.interest(null)
			.userFavor(null)
			.idealType(null)
			.selfIntroduction(null)
			.skill(null)
			.openToWork(null)
			.openToSideProject(null)
			.allowOfficial(false)
			.hasProfile(true)
			.editActivitiesAble(true)
			.openToSoulmate(false)
			.isPhoneBlind(true)
			.links(null)
			.careers(null)
			.voteSelections(null)
			.build();

		memberRepository.save(member);
		return member;
	}

	@Transactional
	public void deleteDefaultMemberProfile(Long userId) {
		if (!memberRepository.existsById(userId)) {
			throw new NotFoundException("해당 id의 Member를 찾을 수 없습니다.");
		}
		memberRepository.deleteById(userId);
	}

	// 프론트 연결하면 삭제 예정
	@Deprecated
	@Transactional
	public Member saveMemberProfile(Long userId, MemberProfileSaveRequest request) {
		val userDetails = platformService.getInternalUser(userId);
		val activityTeamMap = request.activities()
			.stream()
			.collect(Collectors.toMap(MemberProfileSaveRequest.MemberSoptActivitySaveRequest::generation,
				MemberProfileSaveRequest.MemberSoptActivitySaveRequest::team, (team1, team2) -> team1));

		List<PlatformUserUpdateRequest.SoptActivityRequest> soptActivitiesForPlatform = userDetails.soptActivities()
			.stream()
			.map(activity -> new PlatformUserUpdateRequest.SoptActivityRequest(activity.activityId(),
				activityTeamMap.get(activity.generation())))
			.toList();

		val platformRequest = new PlatformUserUpdateRequest(request.name(), request.profileImage(),
			request.birthday() != null ? request.birthday().format(DateTimeFormatter.ISO_LOCAL_DATE) : null,
			request.phone(), request.email(), soptActivitiesForPlatform);

		platformService.updateInternalUser(userId, platformRequest);
		Member member = memberRepository.findById(userId).orElseThrow(() -> new NotFoundException("Member"));
		Long memberId = member.getId();
		if (Objects.isNull(memberId))
			throw new NotFoundException("Member id is null");
		List<MemberLink> memberLinkEntities = request.links()
			.stream()
			.map(link -> MemberLink.builder().memberId(memberId).title(link.title()).url(link.url()).build())
			.collect(Collectors.toList());
		memberLinkEntities.forEach(link -> link.setMemberId(memberId));
		val memberLinks = memberLinkRepository.saveAll(memberLinkEntities);
		val memberCareerEntities = request.careers().stream().map(career -> {
			val formatter = DateTimeFormatter.ofPattern("yyyy-MM");
			try {
				val start = YearMonth.parse(career.startDate(), formatter);
				if (!career.isCurrent()) {
					val end = YearMonth.parse(career.endDate(), formatter);
					if (start.isAfter(end))
						throw new BadRequestException("커리어는 시작 날짜가 더 앞서야 합니다.");
				}
			} catch (DateTimeParseException e) {
				throw new BadRequestException("날짜 형식이 잘못되었습니다.");
			}
			return MemberCareer.builder()
				.memberId(memberId)
				.companyName(career.companyName())
				.title(career.title())
				.startDate(career.startDate())
				.endDate(career.endDate())
				.isCurrent(career.isCurrent())
				.build();
		}).collect(Collectors.toList());
		val memberCareers = memberCareerRepository.saveAll(memberCareerEntities);
		val userFavor = UserFavor.builder()
			.isMintChocoLover(request.userFavor().isMintChocoLover())
			.isSojuLover(request.userFavor().isSojuLover())
			.isPourSauceLover(request.userFavor().isPourSauceLover())
			.isRedBeanFishBreadLover(request.userFavor().isRedBeanFishBreadLover())
			.isRiceTteokLover(request.userFavor().isRiceTteokLover())
			.isHardPeachLover(request.userFavor().isHardPeachLover())
			.build();

		member.saveMemberProfile(request.address(), request.university(), request.major(), request.introduction(),
			request.skill(), request.mbti(), request.mbtiDescription(), request.sojuCapacity(), request.interest(),
			userFavor, request.idealType(), request.selfIntroduction(), request.allowOfficial(), memberLinks,
			memberCareers, request.isPhoneBlind(), null);

		try {
			if (Objects.equals(activeProfile, "prod")) {
				val slackRequest = createSlackRequest(member.getId(), request.name(), request.idealType());
				slackClient.postNewProfileMessage(slackRequest.toString());
			}
		} catch (RuntimeException ex) {
			log.error("슬랙 요청이 실패했습니다 : " + ex.getMessage());
		}
		return member;
	}

	private JsonNode createSlackRequest(Long id, String name, String idealType) {
		val rootNode = slackMessageUtil.getObjectNode();
		rootNode.put("text", "새로운 유저가 프로필을 만들었어요!");

		val blocks = slackMessageUtil.getArrayNode();
		val textField = slackMessageUtil.createTextField("새로운 유저가 프로필을 만들었어요!");
		val contentNode = slackMessageUtil.createSection();

		val fields = slackMessageUtil.getArrayNode();
		fields.add(slackMessageUtil.createTextFieldNode("*이름:*\n" + name));
		fields.add(
			slackMessageUtil.createTextFieldNode("*프로필링크:*\n<https://playground.sopt.org/members/" + id + "|멤버프로필>"));
		fields.add(slackMessageUtil.createTextFieldNode("*이상형:*\n" + idealType));
		contentNode.set("fields", fields);

		blocks.add(textField);
		blocks.add(contentNode);
		rootNode.set("blocks", blocks);
		return rootNode;
	}

	/**
	 * 임원진 직책 여부 확인
	 * - "회장", "부회장", "총무"와 정확히 일치하거나
	 * - "파트장"을 포함하거나 (예: "기획 파트장", "디자인 파트장")
	 * - "팀장"을 포함하면 (예: "운영팀 팀장", "미디어팀 팀장")
	 * 임원진으로 간주
	 */
	private boolean isExecutivePosition(String team) {
		if (team == null || team.isEmpty()) {
			return false;
		}
		return team.equals("회장")
			|| team.equals("부회장")
			|| team.equals("총무")
			|| team.contains("파트장")
			|| team.contains("팀장")
			|| team.equals("아트디렉터");
	}

	/**
	 * 가공된 team 값을 Platform 원본 team 값으로 역변환
	 * PlatformService.convertRoleToTeamValue의 역변환
	 *
	 * - "회장", "부회장", "총무", "파트장" 포함 → null
	 * - "팀장" 포함 (예: "운영팀 팀장") → "운영팀"
	 * - 그 외 → 원본 그대로 반환
	 */
	private String convertTeamToOriginalValue(String team) {
		if (team == null || team.isEmpty()) {
			return null;
		}

		// 회장, 부회장, 총무는 원본 team이 null
		if (team.equals("회장") || team.equals("부회장") || team.equals("총무") || team.equals("아트디렉터")) {
			return null;
		}

		// "기획 파트장", "디자인 파트장" 등 → 원본 team은 null
		if (team.contains("파트장")) {
			return null;
		}

		// "운영팀 팀장", "미디어팀 팀장" 등 → " 팀장" 제거
		if (team.contains("팀장")) {
			return team.replace(" 팀장", "");
		}

		// 일반 팀 (미디어팀, 운영팀 등)은 그대로 반환
		return team;
	}

	@Transactional
	public Member updateMemberProfile(Long id, MemberProfileUpdateRequest request) {
		val userDetails = platformService.getInternalUser(id);

		// 같은 generation에 대한 중복 키 처리 - SOPT 활동 우선
		Map<Integer, SoptActivity> dbActivityMap = userDetails.soptActivities()
			.stream()
			.collect(Collectors.toMap(SoptActivity::generation, Function.identity(), (existing, replacement) -> existing));

		List<PlatformUserUpdateRequest.SoptActivityRequest> soptActivitiesForPlatform = new ArrayList<>();

		for (val requestActivity : request.activities()) {
			SoptActivity dbActivity = dbActivityMap.get(requestActivity.generation());

			if (dbActivity == null) {
				throw new BadRequestException(
					"요청된 활동 기수 정보(" + requestActivity.generation() + ")가 유저의 기존 정보와 일치하지 않습니다.");
			}

			// 임원진 기수는 원본 team 값으로 역변환하여 전송 (업데이트하지 않음)
			if (isExecutivePosition(dbActivity.team())) {
				String originalTeam = convertTeamToOriginalValue(dbActivity.team());
				soptActivitiesForPlatform.add(
					new PlatformUserUpdateRequest.SoptActivityRequest(
						dbActivity.activityId(),
						originalTeam // Platform 원본 team 값으로 역변환
					)
				);
				continue;
			}

			// 일반 팀만 ActivityTeam 검증
			if (!ActivityTeam.hasActivityTeam(requestActivity.team())) {
				throw new BadRequestException("잘못된 솝트 활동 팀 이름입니다.");
			}

			soptActivitiesForPlatform.add(
				new PlatformUserUpdateRequest.SoptActivityRequest(
					dbActivity.activityId(),
					requestActivity.team()
				)
			);
		}
		val platformRequest = new PlatformUserUpdateRequest(request.name(), request.profileImage(),
			request.birthday() != null ? request.birthday().format(DateTimeFormatter.ISO_LOCAL_DATE) : null,
			request.phone() != null && !request.phone().isBlank() ? request.phone() : userDetails.phone(),
			request.email(), soptActivitiesForPlatform);

		platformService.updateInternalUser(id, platformRequest);

		val member = getMemberById(id);
		val memberId = member.getId();
		val memberLinks = memberLinkRepository.saveAll(request.links()
			.stream()
			.map(link -> MemberLink.builder()
				.id(link.id())
				.memberId(memberId)
				.title(link.title())
				.url(link.url())
				.build())
			.collect(Collectors.toList()));

		val memberCareers = request.careers().stream().map(career -> {
			val formatter = DateTimeFormatter.ofPattern("yyyy-MM");
			try {
				val start = YearMonth.parse(career.startDate(), formatter);
				if (!career.isCurrent()) {
					val end = YearMonth.parse(career.endDate(), formatter);
					if (start.isAfter(end))
						throw new BadRequestException("커리어는 시작 날짜가 더 앞서야 합니다.");
				}
			} catch (DateTimeParseException e) {
				throw new BadRequestException("날짜 형식이 잘못되었습니다.");
			}
			return MemberCareer.builder()
				.memberId(memberId)
				.companyName(career.companyName())
				.title(career.title())
				.startDate(career.startDate())
				.endDate(career.endDate())
				.isCurrent(career.isCurrent())
				.build();
		}).collect(Collectors.toList());

		val userFavor = UserFavor.builder()
			.isMintChocoLover(request.userFavor().isMintChocoLover())
			.isSojuLover(request.userFavor().isSojuLover())
			.isPourSauceLover(request.userFavor().isPourSauceLover())
			.isRedBeanFishBreadLover(request.userFavor().isRedBeanFishBreadLover())
			.isRiceTteokLover(request.userFavor().isRiceTteokLover())
			.isHardPeachLover(request.userFavor().isHardPeachLover())
			.build();

		WorkPreference workPreference = null;
		if (request.workPreference() != null) {
			workPreference = workPreferenceModifier.buildWorkPreferenceFromStrings(
				request.workPreference().ideationStyle(),
				request.workPreference().workTime(),
				request.workPreference().communicationStyle(),
				request.workPreference().workPlace(),
				request.workPreference().feedbackStyle()
			);
		}

		member.saveMemberProfile(request.address(), request.university(), request.major(), request.introduction(),
			request.skill(), request.mbti(), request.mbtiDescription(), request.sojuCapacity(), request.interest(),
			userFavor, request.idealType(), request.selfIntroduction(), request.allowOfficial(), memberLinks,
			memberCareers, request.isPhoneBlind(), workPreference);

		return member;
	}

	@Transactional
	public void updateWorkPreference(Long memberId, WorkPreferenceUpdateRequest request) {
		workPreferenceModifier.updateWorkPreference(memberId, request);
	}

	@Transactional(readOnly = true)
	public WorkPreferenceResponse getWorkPreference(Long userId) {
		Member member = getMemberById(userId);
		WorkPreference workPreference = member.getWorkPreference();

		if (workPreference == null) {
			throw new BadRequestException("작업 성향이 설정되지 않았습니다.");
		}

		WorkPreferenceResponse.WorkPreferenceData data = new WorkPreferenceResponse.WorkPreferenceData(
			workPreference.getIdeationStyleValue(),
			workPreference.getWorkTimeValue(),
			workPreference.getCommunicationStyleValue(),
			workPreference.getWorkPlaceValue(),
			workPreference.getFeedbackStyleValue()
		);

		return new WorkPreferenceResponse(data);
	}

	@Transactional(readOnly = true)
	public WorkPreferenceRecommendationResponse getWorkPreferenceRecommendations(Long userId) {
		Member currentMember = getMemberById(userId);
		WorkPreference currentPreference = currentMember.getWorkPreference();

		if (currentPreference == null) {
			return new WorkPreferenceRecommendationResponse(false, Collections.emptyList());
		}

		InternalUserDetails currentUserDetails = platformService.getInternalUser(userId);

		List<Long> appJamObMemberIds = AppJamObMemberId.getAppJamObMember();
		boolean enableWorkPreferenceEvent =
			Objects.equals(currentUserDetails.lastGeneration(), Constant.CURRENT_GENERATION) || appJamObMemberIds.contains(userId);

		if (!enableWorkPreferenceEvent) {
			return new WorkPreferenceRecommendationResponse(true, Collections.emptyList());
		}

		List<Member> membersWithWorkPreference = workPreferenceRetriever.findMembersWithWorkPreference(userId);
		if (membersWithWorkPreference.isEmpty()) {
			return new WorkPreferenceRecommendationResponse(true, Collections.emptyList());
		}

		List<Long> memberIds = membersWithWorkPreference.stream()
				.map(Member::getId)
				.toList();

		Map<Long, InternalUserDetails> latestGenerationUserMap =
				workPreferenceRetriever.getLatestGenerationMembersMap(memberIds);

		List<Member> candidateMembers = membersWithWorkPreference.stream()
				.filter(member -> latestGenerationUserMap.containsKey(member.getId()))
				.toList();

		if (candidateMembers.isEmpty()) {
			return new WorkPreferenceRecommendationResponse(true, Collections.emptyList());
		}

		List<Member> matchedCandidates = workPreferenceRetriever.filterCandidatesByMatchCount(
				candidateMembers, currentPreference, 3);

		List<Member> shuffledCandidates = new ArrayList<>(matchedCandidates);
		Collections.shuffle(shuffledCandidates);
		List<Member> selectedCandidates = shuffledCandidates.stream()
				.limit(4)
				.toList();

		List<WorkPreferenceRecommendationResponse.RecommendedMember> recommendations =
				memberMapper.toRecommendedMembers(
						selectedCandidates,
						latestGenerationUserMap
				);

		return new WorkPreferenceRecommendationResponse(true, recommendations);
	}

	@Transactional
	public void deleteUserProfileLink(Long linkId, Long memberId) {
		MemberLink link = memberLinkRepository.findByIdAndMemberId(linkId, memberId)
			.orElseThrow(() -> new NotFoundException("Member Profile Link"));
		memberLinkRepository.delete(link);
	}

	/*
	 * 플랫폼 팀에서 멤버 활동을 관리하고 있기 때문에, 내부에서 삭제 불가
	 */
	// @Transactional
	// public void deleteUserProfileActivity(Long activityId, Long memberId) {
	// MemberSoptActivity activity =
	// memberSoptActivityRepository.findByIdAndMemberId(activityId, memberId)
	// .orElseThrow(() -> new NotFoundDBEntityException("Member Profile Activity"));
	// memberSoptActivityRepository.delete(activity);
	// }

	@Transactional
	public void checkActivities(Long memberId, Boolean isCheck) {
		Member member = memberRepository.findById(memberId).orElseThrow(() -> new NotFoundException("Member"));

		member.editActivityChange(isCheck);
	}

	@Transactional(readOnly = true)
	public MemberBlockResponse getBlockStatus(Long memberId, Long blockedMemberId) {
		Member blocker = memberRetriever.findMemberById(memberId);
		Member blockedMember = memberRetriever.findMemberById(blockedMemberId);
		InternalUserDetails blockerDetail = platformService.getInternalUser(memberId);
		InternalUserDetails blockedMemberDetail = platformService.getInternalUser(blockedMemberId);

		Optional<MemberBlock> blockHistory = memberBlockRepository.findByBlockerAndBlockedMember(blocker,
			blockedMember);
		return blockHistory.map(
				memberBlock -> MemberBlockResponse.of(memberBlock.getIsBlocked(), blockerDetail, blockedMemberDetail))
			.orElseGet(() -> MemberBlockResponse.of(false, blockerDetail, blockedMemberDetail));
	}

	@Transactional
	public void blockUser(Long memberId, Long blockedMemberId) {
		Member blocker = memberRetriever.findMemberById(memberId);
		Member blockedMember = memberRetriever.findMemberById(blockedMemberId);

		Optional<MemberBlock> blockHistory = memberBlockRepository.findByBlockerAndBlockedMember(blocker,
			blockedMember);
		if (blockHistory.isPresent()) {
			MemberBlock block = blockHistory.get();
			block.updateIsBlocked(true);
			memberBlockRepository.save(block);
		} else {
			MemberBlock newBlock = MemberBlock.builder().blocker(blocker).blockedMember(blockedMember).build();
			memberBlockRepository.save(newBlock);
		}
	}

	@Transactional
	public void reportUser(Long memberId, Long reportedMemberId) {
		InternalUserDetails reporterDetails = platformService.getInternalUser(memberId);
		InternalUserDetails reportedDetails = platformService.getInternalUser(reportedMemberId);

		sendReportToSlack(reporterDetails, reportedDetails);

		Member reporter = memberRetriever.findMemberById(memberId);
		Member reportedMember = memberRetriever.findMemberById(reportedMemberId);

		memberReportRepository.save(MemberReport.builder().reporter(reporter).reportedMember(reportedMember).build());
	}

	@Transactional(readOnly = true)
	public MemberPropertiesResponse getMemberProperties(Long memberId) {
		Member member = memberRetriever.findMemberById(memberId);
		InternalUserDetails userDetails = platformService.getInternalUser(memberId);
		MemberCareer memberCareer = memberCareerRetriever.findMemberLastCareerByMemberId(memberId);
		MemberCoffeeChatPropertyDto coffeeChatProperty = coffeeChatRetriever.getMemberCoffeeChatProperty(member);
		List<String> activitiesAndGeneration = platformService.getPartAndGenerationList(memberId);

		long uploadSopticleCount = communityPostRepository.countSopticleByMemberId(memberId);
		long uploadReviewCount = reviewService.fetchReviewCountByUsername(userDetails.name());

		return memberResponseMapper.toMemberPropertiesResponse(member, memberCareer, coffeeChatProperty,
			activitiesAndGeneration, uploadSopticleCount, uploadReviewCount);
	}

	private void sendReportToSlack(InternalUserDetails reporter, InternalUserDetails reportedMember) {
		try {
			if (Objects.equals(activeProfile, "prod")) {
				val slackRequest = createReportSlackRequest(reporter.userId(), reporter.name(), reportedMember.userId(),
					reportedMember.name());
				slackClient.postReportMessage(slackRequest.toString());
			}
		} catch (RuntimeException ex) {
			log.error("슬랙 요청이 실패했습니다 : " + ex.getMessage());
		}
	}

	private JsonNode createReportSlackRequest(Long blockerId, String blockerName, Long blockedMemberId,
		String blockedMemberName) {
		val rootNode = slackMessageUtil.getObjectNode();
		rootNode.put("text", "🚨유저 신고 발생!🚨");

		val blocks = slackMessageUtil.getArrayNode();
		val textField = slackMessageUtil.createTextField("유저 신고가 들어왔어요!");
		val contentNode = slackMessageUtil.createSection();

		val fields = slackMessageUtil.getArrayNode();
		fields.add(slackMessageUtil.createTextFieldNode("*신고자:*\n" + blockerName + "(id: " + blockerId + ")"));
		fields.add(slackMessageUtil.createTextFieldNode(
			"*신고 당한 유저:*\n" + blockedMemberName + "(id: " + blockedMemberId + ")"
				+ "<https://playground.sopt.org/members/" + blockedMemberId + ">"));
		contentNode.set("fields", fields);

		blocks.add(textField);
		blocks.add(contentNode);
		rootNode.set("blocks", blocks);
		return rootNode;
	}

	private String checkTeamNullCondition(String team) {
		val teamNullCondition = (team == null || team.equals("해당 없음"));
		if (teamNullCondition) {
			team = null;
		}
		return team;
	}

	public List<MakersMemberProfileResponse> getAllMakersMembersProfiles() {
		List<Long> makerMemberIds = MakersMemberId.getMakersMember();
		List<Member> members = memberRepository.findAllByHasProfileTrueAndIdInWithCareers(makerMemberIds);
		List<InternalUserDetails> userDetails = platformService.getInternalUsers(makerMemberIds);

		Map<Long, List<MemberCareer>> careerMap = members.stream()
			.collect(Collectors.toMap(Member::getId, Member::getCareers, (a, b) -> a));

		return userDetails.stream().map(userDetail -> {
			List<MemberSoptActivityResponse> memberSoptActivityResponses = userDetail.soptActivities()
				.stream()
				.sorted(Comparator.comparing(SoptActivity::normalizedGeneration)
					.thenComparing(activity -> !activity.isSopt())) // 같은 기수에서 SOPT(isSopt=true) 우선
				.map(activity -> new MemberSoptActivityResponse((long)activity.activityId(), activity.generation()))
				.toList();
			List<MemberCareer> memberCareers = careerMap.getOrDefault(userDetail.userId(), Collections.emptyList());
			List<MemberCareerResponse> memberCareerResponses = memberCareers.stream()
				.map(memberCareer -> new MemberCareerResponse(memberCareer.getId(), memberCareer.getCompanyName(),
					memberCareer.getTitle(), memberCareer.getIsCurrent()))
				.toList();

			return new MakersMemberProfileResponse(userDetail.userId(), userDetail.name(), userDetail.profileImage(),
				memberSoptActivityResponses, memberCareerResponses);
		}).toList();
	}

    @Transactional(readOnly = true)
    public List<TlMemberResponse> getAppjamTlMembers(Long userId) {
        InternalUserDetails userDetails = platformService.getInternalUser(userId);
        boolean isCurrentGenerationUser = userDetails.soptActivities().stream()
                .anyMatch(activity -> Objects.equals(activity.generation(), Constant.CURRENT_GENERATION));

        if (!isCurrentGenerationUser) {
            throw new BadRequestException("최신 기수가 아닌 유저입니다.");
        }

        List<TlMember> tlMembers = tlMemberRetriever.findByTlGeneration(Constant.CURRENT_GENERATION);

        List<Long> tlMemberIds = tlMembers.stream()
                .map(tlMember -> tlMember.getMember().getId())
                .toList();

        Map<Long, Member> memberById = memberRepository.findAllByHasProfileTrueAndIdIn(tlMemberIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        Map<Long, InternalUserDetails> tlUserDetailsById = platformService.getInternalUsers(tlMemberIds).stream()
                .collect(Collectors.toMap(InternalUserDetails::userId, Function.identity()));

        return tlMembers.stream()
                .filter(tlMember -> {
                    Long memberId = tlMember.getMember().getId();
                    return memberById.containsKey(memberId) && tlUserDetailsById.containsKey(memberId);
                })
                .sorted((tl1, tl2) -> {
                    String name1 = tlUserDetailsById.get(tl1.getMember().getId()).name();
                    String name2 = tlUserDetailsById.get(tl2.getMember().getId()).name();
                    return name1.compareTo(name2);
                })
                .map(tlMember -> {
                    Long memberId = tlMember.getMember().getId();
                    return buildTlMemberResponse(
                            memberId,
                            memberById.get(memberId),
                            tlUserDetailsById.get(memberId),
                            tlMember.getServiceType(),
							tlMember.getSelfIntroduction(),
							tlMember.getCompetitionData()
                    );
                })
                .toList();
    }

    private TlMemberResponse buildTlMemberResponse(
            Long memberId,
            Member member,
            InternalUserDetails userDetails,
			ServiceType serviceType,
			String selfIntroduction,
			String competitionData
    ) {
        List<MemberProfileResponse.MemberSoptActivityResponse> activities = userDetails.soptActivities().stream()
                .sorted(Comparator.comparing(SoptActivity::normalizedGeneration)
                        .thenComparing(activity -> !activity.isSopt())) // 같은 기수에서 SOPT(isSopt=true) 우선
                .map(activity -> new MemberProfileResponse.MemberSoptActivityResponse(
                        (long) activity.activityId(),
                        activity.generation(),
                        activity.part(),
                        activity.team()
                ))
                .toList();

        return new TlMemberResponse(
                memberId,
                userDetails.name(),
                member.getUniversity(),
                userDetails.profileImage(),
                activities,
                member.getIntroduction(),
                serviceType,
				selfIntroduction,
				competitionData
        );
    }

    @Transactional(readOnly = true)
    public AskMemberResponse getAskMembers(String partName) {
        List<AskMemberResponse.QuestionTargetMember> targetMembers = new ArrayList<>();

        Part part = convertToPart(partName);

        // prod 프로파일인지 확인
        boolean isProd = "prod".equals(activeProfile);

        // 특정 파트가 지정된 경우 해당 파트만, 없으면 모든 파트
        List<Long> memberIds;
        if (part != null) {
            memberIds = askMemberId.getAskMembersByPart(part, isProd);
        } else {
            memberIds = askMemberId.getAllAskMembers(isProd);
        }

        for (Long memberId : memberIds) {
            try {
                Member member = memberRepository.findById(memberId).orElse(null);
                if (member == null || !member.getHasProfile()) {
                    continue;
                }

                InternalUserDetails userDetails = platformService.getInternalUser(memberId);

                // 최근 활동 정보 가져오기 - 같은 generation에서 SOPT 우선
                SoptActivity latestActivity = userDetails.soptActivities().stream()
                        .max(Comparator.comparing(SoptActivity::normalizedGeneration)
                                .thenComparing(SoptActivity::isSopt)) // SOPT(true) 우선
                        .orElse(null);

                if (latestActivity == null) {
                    continue;
                }

                // 커리어 정보 처리 - 현재 직장 우선, 없으면 가장 최근 직장
                AskMemberResponse.AskMemberCareerResponse career = null;

                List<MemberCareer> careers = member.getCareers();
                if (careers != null && !careers.isEmpty()) {
                    // 1. 현재 재직중인 커리어 찾기
                    Optional<MemberCareer> currentCareerOpt = careers.stream()
                            .filter(c -> c.getIsCurrent() != null && c.getIsCurrent())
                            .findFirst();

                    if (currentCareerOpt.isPresent()) {
                        MemberCareer current = currentCareerOpt.get();
                        career = new AskMemberResponse.AskMemberCareerResponse(
                                current.getCompanyName(),
                                current.getTitle()
                        );
                    } else {
                        // 2. 현재 직장이 없으면 가장 최근 직장 찾기
                        MemberCareer mostRecent = careers.stream()
                                .filter(c -> c.getEndDate() != null)
                                .max((c1, c2) -> {
                                    try {
                                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                                        val end1 = YearMonth.parse(c1.getEndDate(), formatter);
                                        val end2 = YearMonth.parse(c2.getEndDate(), formatter);
                                        return end1.compareTo(end2);
                                    } catch (Exception e) {
                                        // 날짜 파싱 실패시 startDate로 비교
                                        try {
                                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                                            val start1 = YearMonth.parse(c1.getStartDate(), formatter);
                                            val start2 = YearMonth.parse(c2.getStartDate(), formatter);
                                            return start1.compareTo(start2);
                                        } catch (Exception ex) {
                                            return 0;
                                        }
                                    }
                                })
                                .orElse(careers.get(0)); // 정렬 실패시 첫 번째 커리어 선택

                        if (mostRecent != null) {
                            career = new AskMemberResponse.AskMemberCareerResponse(
                                    mostRecent.getCompanyName(),
                                    mostRecent.getTitle()
                            );
                        }
                    }
                }

                AskMemberResponse.AskMemberSoptActivityResponse activityResponse =
                        new AskMemberResponse.AskMemberSoptActivityResponse(
                                latestActivity.generation(),
                                latestActivity.part(),
                                latestActivity.team()
                        );

                AskMemberResponse.QuestionTargetMember targetMember =
                        new AskMemberResponse.QuestionTargetMember(
                                memberId,
                                userDetails.name(),
                                userDetails.profileImage(),
                                member.getIntroduction(),
                                activityResponse,
                                career,
                                true  // 답변보장 항상 true
                        );

                targetMembers.add(targetMember);
            } catch (Exception e) {
                log.error("Failed to process member ID: " + memberId, e);
                continue;
            }
        }

        return new AskMemberResponse(targetMembers);
    }

    private Part convertToPart(String partName) {
        if (partName == null || partName.isBlank()) {
            return null;
        }

        return switch (partName.toUpperCase()) {
            case "서버", "SERVER" -> Part.SERVER;
            case "IOS" -> Part.IOS;
            case "안드로이드", "ANDROID" -> Part.ANDROID;
            case "웹", "WEB" -> Part.WEB;
            case "디자인", "DESIGN" -> Part.DESIGN;
            case "기획", "PLAN" -> Part.PLAN;
            default -> null;
        };
    }

}
