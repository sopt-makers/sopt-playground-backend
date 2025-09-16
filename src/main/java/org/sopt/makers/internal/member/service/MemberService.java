package org.sopt.makers.internal.member.service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sopt.makers.internal.coffeechat.dto.request.MemberCoffeeChatPropertyDto;
import org.sopt.makers.internal.coffeechat.service.CoffeeChatRetriever;
import org.sopt.makers.internal.community.repository.post.CommunityPostRepository;
import org.sopt.makers.internal.community.service.ReviewService;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.MemberHasNotProfileException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.platform.PlatformUserUpdateRequest;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.external.platform.UserSearchResponse;
import org.sopt.makers.internal.external.slack.SlackClient;
import org.sopt.makers.internal.external.slack.SlackMessageUtil;
import org.sopt.makers.internal.member.domain.MakersMemberId;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberBlock;
import org.sopt.makers.internal.member.domain.MemberCareer;
import org.sopt.makers.internal.member.domain.MemberLink;
import org.sopt.makers.internal.member.domain.MemberReport;
import org.sopt.makers.internal.member.domain.UserFavor;
import org.sopt.makers.internal.member.domain.enums.OrderByCondition;
import org.sopt.makers.internal.member.dto.ActivityVo;
import org.sopt.makers.internal.member.dto.MemberProfileProjectDao;
import org.sopt.makers.internal.member.dto.MemberProfileProjectVo;
import org.sopt.makers.internal.member.dto.request.MemberProfileSaveRequest;
import org.sopt.makers.internal.member.dto.request.MemberProfileUpdateRequest;
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
import org.sopt.makers.internal.member.mapper.MemberMapper;
import org.sopt.makers.internal.member.mapper.MemberResponseMapper;
import org.sopt.makers.internal.member.repository.MemberBlockRepository;
import org.sopt.makers.internal.member.repository.MemberLinkRepository;
import org.sopt.makers.internal.member.repository.MemberProfileQueryRepository;
import org.sopt.makers.internal.member.repository.MemberReportRepository;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.sopt.makers.internal.member.repository.career.MemberCareerRepository;
import org.sopt.makers.internal.member.service.career.MemberCareerRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
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
	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Transactional(readOnly = true)
	public MemberInfoResponse getMyInformation(Long userId) {
		Member member = getMemberById(userId);
		InternalUserDetails userDetails = platformService.getInternalUser(userId);
		boolean isCoffeeChatActive = coffeeChatRetriever.existsCoffeeChat(member);
		return memberResponseMapper.toMemberInfoResponse(member, userDetails, isCoffeeChatActive);
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
		boolean isMine = Objects.equals(profileId, viewerId);
		boolean isCoffeeChatActivate = coffeeChatRetriever.existsCoffeeChat(member);
		MemberProfileSpecificResponse response = memberMapper.toProfileSpecificResponse(member, userDetails, isMine,
			memberProfileProjects, activityResponses, isCoffeeChatActivate);

		Map<Integer, MemberProfileProjectVo> projectMap = soptActivityResponse.stream()
			.collect(Collectors.toMap(MemberProfileProjectVo::generation, vo -> vo));

		// soptActivities 갱신 (response.soptActivities()에 soptActivityResponse 붙히기)
		List<MemberProfileSpecificResponse.SoptMemberActivityResponse> updatedActivities = response.soptActivities()
			.stream()
			.map(sa -> {
				MemberProfileProjectVo matched = projectMap.get(sa.generation());
				if (matched != null) {
					return new MemberProfileSpecificResponse.SoptMemberActivityResponse(sa.generation(), sa.part(),
						sa.team(), matched.projects());
				}
				return sa;
			})
			.toList();

		// updatedActivities set 해주기
		MemberProfileSpecificResponse updateResponse = new MemberProfileSpecificResponse(response.name(),
			response.profileImage(), response.birthday(), response.isPhoneBlind(), response.phone(), response.email(),
			response.address(), response.university(), response.major(), response.introduction(), response.skill(),
			response.mbti(), response.mbtiDescription(), response.sojuCapacity(), response.interest(),
			response.userFavor(), response.idealType(), response.selfIntroduction(), response.activities(),
			updatedActivities, response.links(), response.projects(), response.careers(), response.allowOfficial(),
			response.isCoffeeChatActivate(), response.isMine());
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
			.orElseThrow(() -> new NotFoundDBEntityException("해당 id의 Member를 찾을 수 없습니다."));
		if (member.getHasProfile())
			return member;
		else
			throw new MemberHasNotProfileException("해당 Member는 프로필이 없습니다.");
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
			throw new NotFoundDBEntityException("30기 이전 기수 활동 회원은 공식 채널로 문의해주시기 바랍니다.");
		}

		val cardinalInfoMap = soptActivities.stream()
			.collect(Collectors.toMap(SoptActivity::generation, SoptActivity::part, (p1, p2) -> p1));

		val activities = soptActivities.stream().map(a -> memberMapper.toActivityInfoVo(a, false));

		val projects = memberProfileProjects.stream().filter(p -> p.generation() != null).map(p -> {
			val part = cardinalInfoMap.getOrDefault(p.generation(), "");
			return memberMapper.toActivityInfoVo(p, true, part);
		});

		val genActivityMap = Stream.concat(activities, projects).collect(Collectors.groupingBy(ActivityVo::generation));

		return genActivityMap.entrySet()
			.stream()
			.collect(Collectors.toMap(e -> e.getKey() + "," + cardinalInfoMap.getOrDefault(e.getKey(), ""),
				Map.Entry::getValue));
	}

	public List<MemberProfileProjectVo> getMemberProfileProjects(List<SoptActivity> soptActivities,
		List<MemberProfileProjectDao> memberProfileProjects) {
		return soptActivities.stream().map(m -> {
			val projects = memberProfileProjects.stream()
				.filter(p -> p.generation() != null)
				.filter(p -> p.generation().equals(m.generation()))
				.map(memberMapper::toActivityInfoVo)
				.collect(Collectors.toList());
			return memberMapper.toSoptMemberProfileProjectVo(m, projects);
		}).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public MemberAllProfileResponse getMemberProfiles(Integer filter, Integer limit, Integer cursor, String search,
		Integer generation, Integer employed, Integer orderBy, String mbti, String team) {
		// 1) DB에서 먼저 서버 필터(mbti, employed, university/company)로 해당하는 모든 userId 조회
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
			.filter(u -> filterPlatformConditions(u, part, checkedTeam, generation, search))
			.toList();

		if (filteredByPlatform.isEmpty()) {
			return new MemberAllProfileResponse(Collections.emptyList(), false, 0);
		}

		// 3) DB 멤버 로드 및 응답 매핑
		Map<Long, Member> memberMap = memberRepository.findAllByIdIn(
			filteredByPlatform.stream().map(InternalUserDetails::userId).toList()
		).stream().collect(Collectors.toMap(Member::getId, Function.identity()));

		// 3-1) 서버에서 페이지네이션 처리 (cursor: id 커서, limit: 개수)
		// 입력 cursor가 있으면 해당 id보다 작은(id < cursor) 최신순 기준으로 슬라이스, 없으면 최신순 기준 상위 limit
		Long cursorId = (cursor == null || cursor == 0) ? null : cursor.longValue();
		List<InternalUserDetails> pagedByServer = filteredByPlatform.stream()
			.sorted((a, b) -> Long.compare(b.userId(), a.userId()))
			.filter(u -> cursorId == null || u.userId() < cursorId)
			.limit(limit == null || limit <= 0 ? 30 : limit)
			.toList();

		if (pagedByServer.isEmpty()) {
			return new MemberAllProfileResponse(Collections.emptyList(), false, 0);
		}

		List<MemberProfileResponse> memberList = pagedByServer.stream().map(userDetails -> {
			Member member = memberMap.get(userDetails.userId());
			boolean isCoffeeChatActivate = member != null && coffeeChatRetriever.existsCoffeeChat(member);
			return memberMapper.toProfileResponse(member, userDetails, isCoffeeChatActivate);
		}).toList();

		// 4) hasNext 및 totalCount 계산 (서버 기준)
		boolean hasNext = filteredByPlatform.stream()
			.anyMatch(u -> u.userId() < pagedByServer.get(pagedByServer.size() - 1).userId());
		int totalCount = filteredByPlatform.size();

		return new MemberAllProfileResponse(memberList, hasNext, totalCount);
	}

	private boolean filterPlatformConditions(InternalUserDetails userDetails, String part, String team, Integer generation, String nameSearch) {
		// 이름 검색은 플랫폼 이름에도 적용
		if (nameSearch != null && !nameSearch.isBlank()) {
			if (!userDetails.name().contains(nameSearch)) {
				return false;
			}
		}
		if (part == null && team == null && generation == null) return true;
		List<SoptActivity> activities = userDetails.soptActivities();
		return activities.stream().anyMatch(a ->
				(generation == null || Objects.equals(a.generation(), generation)) &&
				(part == null || Objects.equals(a.part(), part)) &&
				(team == null || Objects.equals(a.team(), team))
		);
	}

	private boolean isCurrentlyEmployed(List<MemberCareer> careers, int employedStatus) {
		boolean isWorking = careers.stream().anyMatch(MemberCareer::getIsCurrent);
		return (employedStatus == 1) == isWorking;
	}

	private String getMemberPart(Integer filter) {
		if (filter == null)
			return null;
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

	private String convertOrderBy(Integer orderBy) {
		if (orderBy == null)
			return OrderByCondition.LATEST_REGISTERED.name();
		return switch (orderBy) {
			case 1 -> OrderByCondition.LATEST_REGISTERED.name();
			case 2 -> OrderByCondition.OLDEST_REGISTERED.name();
			case 3 -> OrderByCondition.LATEST_GENERATION.name();
			case 4 -> OrderByCondition.OLDEST_GENERATION.name();
			default -> OrderByCondition.LATEST_REGISTERED.name();
		};
	}

	private String checkActivityTeamConditions(String team) {
		Predicate<String> teamIsEmpty = Objects::isNull;
		Predicate<String> teamIsNullString = s -> s.equals("해당 없음");
		val isNullResult = teamIsEmpty.or(teamIsNullString).test(team);
		if (isNullResult)
			return null;
		else
			return team;
	}

	public Member saveDefaultMemberProfile(Long userId) {
		if (memberRepository.existsById(userId)) {
			throw new DuplicateKeyException("이미 존재하는 유저입니다. userId=" + userId);
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
			throw new NotFoundDBEntityException("해당 id의 Member를 찾을 수 없습니다.");
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
		Member member = memberRepository.findById(userId).orElseThrow(() -> new NotFoundDBEntityException("Member"));
		Long memberId = member.getId();
		if (Objects.isNull(memberId))
			throw new NotFoundDBEntityException("Member id is null");
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
						throw new ClientBadRequestException("커리어는 시작 날짜가 더 앞서야 합니다.");
				}
			} catch (DateTimeParseException e) {
				throw new ClientBadRequestException("날짜 형식이 잘못되었습니다.");
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
			memberCareers, request.isPhoneBlind());

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

	@Transactional
	public Member updateMemberProfile(Long id, MemberProfileUpdateRequest request) {
		val userDetails = platformService.getInternalUser(id);

		Map<Integer, SoptActivity> dbActivityMap = userDetails.soptActivities()
			.stream()
			.collect(Collectors.toMap(SoptActivity::generation, Function.identity()));

		List<PlatformUserUpdateRequest.SoptActivityRequest> soptActivitiesForPlatform = request.activities()
			.stream()
			.map(requestActivity -> {
				SoptActivity dbActivity = dbActivityMap.get(requestActivity.generation());

				if (dbActivity == null) {
					throw new ClientBadRequestException(
						"요청된 활동 기수 정보(" + requestActivity.generation() + ")가 유저의 기존 정보와 일치하지 않습니다.");
				}

				return new PlatformUserUpdateRequest.SoptActivityRequest(dbActivity.activityId(),
					requestActivity.team());
			})
			.toList();

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
						throw new ClientBadRequestException("커리어는 시작 날짜가 더 앞서야 합니다.");
				}
			} catch (DateTimeParseException e) {
				throw new ClientBadRequestException("날짜 형식이 잘못되었습니다.");
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

		member.saveMemberProfile(request.address(), request.university(), request.major(), request.introduction(),
			request.skill(), request.mbti(), request.mbtiDescription(), request.sojuCapacity(), request.interest(),
			userFavor, request.idealType(), request.selfIntroduction(), request.allowOfficial(), memberLinks,
			memberCareers, request.isPhoneBlind());

		return member;
	}

	@Transactional
	public void deleteUserProfileLink(Long linkId, Long memberId) {
		MemberLink link = memberLinkRepository.findByIdAndMemberId(linkId, memberId)
			.orElseThrow(() -> new NotFoundDBEntityException("Member Profile Link"));
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
		Member member = memberRepository.findById(memberId).orElseThrow(() -> new NotFoundDBEntityException("Member"));

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
		List<Member> members = memberRepository.findAllByHasProfileTrueAndIdIn(makerMemberIds);
		List<InternalUserDetails> userDetails = platformService.getInternalUsers(makerMemberIds);

		Map<Long, List<MemberCareer>> careerMap = members.stream()
			.collect(Collectors.toMap(Member::getId, Member::getCareers, (a, b) -> a));

		return userDetails.stream().map(userDetail -> {
			List<MemberSoptActivityResponse> memberSoptActivityResponses = userDetail.soptActivities()
				.stream()
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

}
