package org.sopt.makers.internal.member.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.coffeechat.dto.request.MemberCoffeeChatPropertyDto;
import org.sopt.makers.internal.coffeechat.service.CoffeeChatRetriever;
import org.sopt.makers.internal.common.util.InfiniteScrollUtil;
import org.sopt.makers.internal.community.repository.post.CommunityPostRepository;
import org.sopt.makers.internal.community.service.ReviewService;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.MemberHasNotProfileException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.platform.PlatformUserUpdateRequest;
import org.sopt.makers.internal.external.slack.SlackClient;
import org.sopt.makers.internal.external.slack.SlackMessageUtil;
import org.sopt.makers.internal.member.domain.MakersMemberId;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberBlock;
import org.sopt.makers.internal.member.domain.MemberCareer;
import org.sopt.makers.internal.member.domain.MemberLink;
import org.sopt.makers.internal.member.domain.MemberReport;
import org.sopt.makers.internal.member.domain.MemberSoptActivity;
import org.sopt.makers.internal.member.domain.UserFavor;
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
import org.sopt.makers.internal.member.repository.soptactivity.MemberSoptActivityRepository;
import org.sopt.makers.internal.member.service.career.MemberCareerRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {
    @Value("${spring.profiles.active}")
    private String activeProfile;
    private final MemberRetriever memberRetriever;
    private final CoffeeChatRetriever coffeeChatRetriever;
    private final MemberCareerRetriever memberCareerRetriever;
    private final MemberResponseMapper memberResponseMapper;
    private final MemberRepository memberRepository;
    private final MemberLinkRepository memberLinkRepository;
    private final MemberSoptActivityRepository memberSoptActivityRepository;
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
    private final InfiniteScrollUtil infiniteScrollUtil;

    @Transactional(readOnly = true)
    public MemberInfoResponse getMyInformation(Long userId) {
        Member member = getMemberById(userId);
        InternalUserDetails userDetails = platformService.getInternalUser(userId);
        boolean isCoffeeChatActive = coffeeChatRetriever.existsCoffeeChat(member);
        return memberResponseMapper.toMemberInfoResponse(member, userDetails, isCoffeeChatActive);
    }

//    @Transactional(readOnly = true)
//    public List<MemberResponse> getMemberByName(String name) {
//        // TODO: - name이 이름에 포함된 멤버 리스트 받아오기 수정 필요해보임
//        List<Member> members = memberRepository.findAllByNameContaining(name);
//        if (members.isEmpty()) {
//            return Collections.emptyList();
//        }
//        List<Long> userIds = members.stream().map(Member::getId).collect(Collectors.toList());
//        Map<Long, InternalUserDetails> userDetailsMap = platformService.getInternalUsers(userIds).stream()
//                .collect(Collectors.toMap(InternalUserDetails::userId, Function.identity()));
//
//        return members.stream().map(member -> {
//            InternalUserDetails userDetails = userDetailsMap.get(member.getId());
//            if (userDetails == null) return null;
//            return new MemberResponse(
//                    member.getId(),
//                    userDetails.name(),
//                    userDetails.lastGeneration(),
//                    userDetails.profileImage(),
//                    member.getHasProfile(),
//                    member.getEditActivitiesAble()
//            );
//        }).filter(Objects::nonNull).collect(Collectors.toList());
//    }

    @Transactional(readOnly = true)
    public MemberProfileSpecificResponse getMemberProfile(Long profileId, Long viewerId) {
        Member member = getMemberHasProfileById(profileId);
        InternalUserDetails userDetails = platformService.getInternalUser(profileId);
        List<MemberProfileProjectDao> memberProfileProjects = getMemberProfileProjects(profileId);
        Map<String, List<ActivityVo>> activityMap = getMemberProfileActivity(member.getActivities(), memberProfileProjects);
        List<MemberProfileProjectVo> soptActivity = getMemberProfileProjects(member.getActivities(), memberProfileProjects);
        List<MemberProfileProjectVo> soptActivityResponse = soptActivity.stream()
                .map(m -> new MemberProfileProjectVo(m.id(), m.generation(), m.part(), checkTeamNullCondition(m.team()),
                        m.projects()))
                .collect(Collectors.toList());
        List<MemberActivityResponse> activityResponses = activityMap.entrySet().stream()
                .map(entry -> new MemberActivityResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        boolean isMine = Objects.equals(profileId, viewerId);
        boolean isCoffeeChatActivate = coffeeChatRetriever.existsCoffeeChat(member);
        MemberProfileSpecificResponse response = memberMapper.toProfileSpecificResponse(
                member, userDetails, isMine, memberProfileProjects,
                activityResponses, soptActivityResponse, isCoffeeChatActivate
        );

        return MemberProfileSpecificResponse.applyPhoneMasking(response, isMine, isCoffeeChatActivate);
    }

    @Transactional(readOnly = true)
    public Member getMemberById(Long id) {
        return memberRetriever.findMemberById(id);
    }

    public MemberResponse getMemberResponseById(Long id) {
        InternalUserDetails user = platformService.getInternalUser(id);
        Member member = getMemberById(id);

        return new MemberResponse(id, user.name(), user.lastGeneration(), user.profileImage(),
                member.getHasProfile(), member.getEditActivitiesAble());
    }

    @Transactional(readOnly = true)
    public Member getMemberHasProfileById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundDBEntityException("해당 id의 Member를 찾을 수 없습니다."));
        if (member.getHasProfile()) return member;
        else throw new MemberHasNotProfileException("해당 Member는 프로필이 없습니다.");
    }

    @Transactional(readOnly = true)
    public List<Member> getMemberProfileListById(String idList) {
        List<Member> members = new ArrayList<>();

        for (Long id : Arrays.stream(URLDecoder.decode(idList, StandardCharsets.UTF_8).split(",")).mapToLong(Long::parseLong).toArray()) {
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

    public Map<String, List<ActivityVo>> getMemberProfileActivity(
            List<MemberSoptActivity> memberActivities,
            List<MemberProfileProjectDao> memberProfileProjects
    ) {
        if (memberActivities.isEmpty()) {
            throw new NotFoundDBEntityException("30기 이전 기수 활동 회원은 공식 채널로 문의해주시기 바랍니다.");
        }

        val cardinalInfoMap = memberActivities.stream()
                .collect(Collectors.toMap(
                        MemberSoptActivity::getGeneration,
                        MemberSoptActivity::getPart,
                        (p1, p2) -> p1)
                );
        val activities = memberActivities.stream().map(a -> memberMapper.toActivityInfoVo(a, false));
        val projects = memberProfileProjects.stream()
                .filter(p -> p.generation() != null)
                .map(p -> {
                    val part = cardinalInfoMap.getOrDefault(p.generation(), "");
                    return memberMapper.toActivityInfoVo(p, true, part);
                });
        val genActivityMap = Stream.concat(activities, projects)
                .collect(Collectors.groupingBy(ActivityVo::generation));
        return genActivityMap.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey() + "," + cardinalInfoMap.getOrDefault(e.getKey(), ""),
                        Map.Entry::getValue
                ));
    }

    public Map<String, List<ActivityVo>> getMemberProfileList(
            List<MemberSoptActivity> memberActivities
    ) {
        val cardinalInfoMap = memberActivities.stream()
                .collect(Collectors.toMap(
                        MemberSoptActivity::getGeneration,
                        MemberSoptActivity::getPart,
                        (p1, p2) -> p1)
                );
        val activities = memberActivities.stream().map(a -> memberMapper.toActivityInfoVo(a, false));
        val genActivityMap = activities.collect(Collectors.groupingBy(ActivityVo::generation));
        return genActivityMap.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey() + "," + cardinalInfoMap.getOrDefault(e.getKey(), ""),
                        Map.Entry::getValue
                ));
    }

    public List<MemberProfileProjectVo> getMemberProfileProjects(
            List<MemberSoptActivity> memberActivities,
            List<MemberProfileProjectDao> memberProfileProjects
    ) {
        return memberActivities.stream()
                .map(m -> {
                    val projects = memberProfileProjects.stream()
                            .filter(p -> p.generation() != null)
                            .filter(p -> p.generation().equals(m.getGeneration()))
                            .map(memberMapper::toActivityInfoVo).collect(Collectors.toList());
                    return memberMapper.toSoptMemberProfileProjectVo(m, projects);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MemberAllProfileResponse getMemberProfiles(
            Integer filter, Integer limit, Integer cursor, String search,
            Integer generation, Integer employed, Integer orderBy, String mbti, String team
    ) {
        List<Member> members = memberProfileQueryRepository.findAllLimitedMemberProfile(
                    getMemberPart(filter), infiniteScrollUtil.checkLimitForPagination(limit),
                    cursor, search, generation, employed, orderBy, mbti, team);

        // 07.21.13:50 추가 - checkContainsSearchCond() name 검색해결 위해 플랫폼팀에서 받아온 정보로 필터링
        List<Long> userIds = members.stream().map(Member::getId).collect(Collectors.toList());
        Map<Long, InternalUserDetails> userDetailsMap = platformService.getInternalUsers(userIds).stream()
                .collect(Collectors.toMap(InternalUserDetails::userId, Function.identity()));
        if (search != null && !search.isBlank()) {
            members = members.stream()
                    .filter(dto -> {
                        InternalUserDetails userDetails = userDetailsMap.get(dto.getId());
                        return userDetails.name().contains(search);
                    }).toList();
        }

        if (members.isEmpty()) {
            return new MemberAllProfileResponse(Collections.emptyList(), false, 0);
        }

        List<MemberProfileResponse> memberList = members.stream().map(member -> {
            InternalUserDetails userDetails = userDetailsMap.get(member.getId());
            if (Objects.isNull(userDetails)) {
                return null;
            }
            boolean isCoffeeChatActivate = coffeeChatRetriever.existsCoffeeChat(member);

            MemberProfileResponse profileResponse = memberMapper.toProfileResponse(member, userDetails, isCoffeeChatActivate);

            return MemberProfileResponse.checkIsBlindPhone(
                    profileResponse,
                    memberMapper.mapPhoneIfBlind(member.getIsPhoneBlind(), userDetails.phone())
            );
        }).filter(Objects::nonNull).collect(Collectors.toList());

        boolean hasNextMember = infiniteScrollUtil.checkHasNextElement(limit, memberList);

        // TODO : - countAllMemberProfile() 아래 메소드 해결 필요해보임.
        int totalMembersCount = memberProfileQueryRepository.countAllMemberProfile(getMemberPart(filter), search, generation, employed, mbti, team);
        return new MemberAllProfileResponse(memberList, hasNextMember, totalMembersCount);
    }

    private String getMemberPart(Integer filter) {
        if (filter == null) return null;
        return switch (filter) {
            case 1 -> "기획";
            case 2 -> "디자인";
            case 3 -> "웹";
            case 4 -> "서버";
            case 5 -> "안드로이드";
            case 6 -> "iOS";
            default -> null;
        };
    }

    private String checkActivityTeamConditions(String team) {
        Predicate<String> teamIsEmpty = Objects::isNull;
        Predicate<String> teamIsNullString = s -> s.equals("해당 없음");
        val isNullResult = teamIsEmpty.or(teamIsNullString).test(team);
        if (isNullResult) return null;
        else return team;
    }

//    @Transactional(readOnly = true)
//    public List<InternalCoffeeChatMemberDto> getAllMemberByCoffeeChatActivate() {
//        List<Member> members = memberRetriever.findAllMembersByCoffeeChatActivate();
//        return members.stream().map(member -> InternalCoffeeChatMemberDto.of(
//                member, platformService.getPartAndGenerationList(member.getId())
//        )).toList();
//    }

    @Transactional
    public Member saveMemberProfile(Long userId, MemberProfileSaveRequest request) {
        val userDetails = platformService.getInternalUser(userId);
        val activityTeamMap = request.activities().stream()
                .collect(Collectors.toMap(
                        MemberProfileSaveRequest.MemberSoptActivitySaveRequest::generation,
                        MemberProfileSaveRequest.MemberSoptActivitySaveRequest::team,
                        (team1, team2) -> team1
                ));

        List<PlatformUserUpdateRequest.SoptActivityRequest> soptActivitiesForPlatform = userDetails.soptActivities().stream()
                .map(activity -> new PlatformUserUpdateRequest.SoptActivityRequest(
                        activity.activityId(),
                        activityTeamMap.get(activity.generation())
                ))
                .toList();

        val platformRequest = new PlatformUserUpdateRequest(
                request.name(),
                request.profileImage(),
                request.birthday() != null ? request.birthday().format(DateTimeFormatter.ISO_LOCAL_DATE) : null,
                request.phone(),
                request.email(),
                soptActivitiesForPlatform
        );

        platformService.updateInternalUser(userId, platformRequest);
        Member member = memberRepository.findById(userId)
                .orElseThrow(
                        () -> new NotFoundDBEntityException("Member")
                );
        Long memberId = member.getId();
        if (Objects.isNull(memberId)) throw new NotFoundDBEntityException("Member id is null");
        List<MemberLink> memberLinkEntities = request.links().stream().map(link ->
                MemberLink.builder()
                        .memberId(memberId)
                        .title(link.title())
                        .url(link.url())
                        .build()).collect(Collectors.toList());
        memberLinkEntities.forEach(link -> link.setMemberId(memberId));
        val memberLinks = memberLinkRepository.saveAll(memberLinkEntities);

        val memberActivities = memberSoptActivityRepository.findAllByMemberId(memberId).stream().map(activity -> {
            val sameGenerationInActivity = request.activities().stream()
                    .filter(activitySaveRequest -> Objects.equals(activitySaveRequest.generation(), activity.getGeneration())).findFirst();
            if (sameGenerationInActivity.isPresent()) {
                val team = sameGenerationInActivity.map(MemberProfileSaveRequest.MemberSoptActivitySaveRequest::team).orElse(null);
                return MemberSoptActivity.builder()
                        .memberId(activity.getMemberId())
                        .part(activity.getPart())
                        .generation(activity.getGeneration())
                        .team(checkActivityTeamConditions(team)).build();
            }
            return MemberSoptActivity.builder()
                    .memberId(activity.getMemberId())
                    .part(activity.getPart())
                    .generation(activity.getGeneration())
                    .team(checkActivityTeamConditions(activity.getTeam())).build();
        }).collect(Collectors.toList());

        val nnActivities = memberActivities.stream().filter(l -> l.getMemberId() != null).count();
        if (nnActivities == 0) throw new NotFoundDBEntityException("There's no activities with memberId");

        val memberCareerEntities = request.careers().stream().map(career -> {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            try {
                val start = YearMonth.parse(career.startDate(), formatter);
                if (!career.isCurrent()) {
                    val end = YearMonth.parse(career.endDate(), formatter);
                    if (start.isAfter(end)) throw new ClientBadRequestException("커리어는 시작 날짜가 더 앞서야 합니다.");
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
        val userFavor = UserFavor.builder().isMintChocoLover(request.userFavor().isMintChocoLover())
                .isSojuLover(request.userFavor().isSojuLover())
                .isPourSauceLover(request.userFavor().isPourSauceLover())
                .isRedBeanFishBreadLover(request.userFavor().isRedBeanFishBreadLover())
                .isRiceTteokLover(request.userFavor().isRiceTteokLover())
                .isHardPeachLover(request.userFavor().isHardPeachLover())
                .build();

        member.saveMemberProfile(
                request.address(), request.university(), request.major(), request.introduction(),
                request.skill(), request.mbti(), request.mbtiDescription(), request.sojuCapacity(),
                request.interest(), userFavor, request.idealType(),
                request.selfIntroduction(), request.allowOfficial(),
                memberActivities, memberLinks, memberCareers, request.isPhoneBlind()
        );

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
        fields.add(slackMessageUtil.createTextFieldNode("*프로필링크:*\n<https://playground.sopt.org/members/" + id + "|멤버프로필>"));
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
        val activityTeamMap = request.activities().stream()
                .collect(Collectors.toMap(
                        MemberProfileUpdateRequest.MemberSoptActivityUpdateRequest::generation,
                        MemberProfileUpdateRequest.MemberSoptActivityUpdateRequest::team,
                        (team1, team2) -> team1
                ));

        List<PlatformUserUpdateRequest.SoptActivityRequest> soptActivitiesForPlatform = userDetails.soptActivities().stream()
                .map(activity -> new PlatformUserUpdateRequest.SoptActivityRequest(
                        activity.activityId(),
                        activityTeamMap.get(activity.generation())
                ))
                .toList();

        val platformRequest = new PlatformUserUpdateRequest(
                request.name(),
                request.profileImage(),
                request.birthday() != null ? request.birthday().format(DateTimeFormatter.ISO_LOCAL_DATE) : null,
                request.phone(),
                request.email(),
                soptActivitiesForPlatform
        );

        platformService.updateInternalUser(id, platformRequest);

        val member = getMemberById(id);

        if (!member.getEditActivitiesAble()) {
            if (!request.compareProfileActivities(request.activities(), member.getActivities())) {
                throw new ClientBadRequestException("이미 프로필을 수정한 적이 있는 유저입니다.");
            }
        }

        val memberId = member.getId();
        val memberLinks = memberLinkRepository.saveAll(
                request.links().stream().map(link ->
                        MemberLink.builder()
                                .id(link.id())
                                .memberId(memberId)
                                .title(link.title())
                                .url(link.url())
                                .build()).collect(Collectors.toList())
        );

        val memberActivities = memberSoptActivityRepository.saveAll(
                request.activities().stream().map(activity ->
                        MemberSoptActivity.builder()
                                .memberId(memberId)
                                .part(activity.part())
                                .generation(activity.generation())
                                .team(checkActivityTeamConditions(activity.team()))
                                .build()).collect(Collectors.toList())
        );

        val memberCareers = request.careers().stream().map(career -> {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            try {
                val start = YearMonth.parse(career.startDate(), formatter);
                if (!career.isCurrent()) {
                    val end = YearMonth.parse(career.endDate(), formatter);
                    if (start.isAfter(end)) throw new ClientBadRequestException("커리어는 시작 날짜가 더 앞서야 합니다.");
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

        val userFavor = UserFavor.builder().isMintChocoLover(request.userFavor().isMintChocoLover())
                .isSojuLover(request.userFavor().isSojuLover())
                .isPourSauceLover(request.userFavor().isPourSauceLover())
                .isRedBeanFishBreadLover(request.userFavor().isRedBeanFishBreadLover())
                .isRiceTteokLover(request.userFavor().isRiceTteokLover())
                .isHardPeachLover(request.userFavor().isHardPeachLover())
                .build();

        member.saveMemberProfile(
                request.address(), request.university(), request.major(), request.introduction(),
                request.skill(), request.mbti(), request.mbtiDescription(), request.sojuCapacity(),
                request.interest(), userFavor, request.idealType(),
                request.selfIntroduction(), request.allowOfficial(),
                memberActivities, memberLinks, memberCareers, request.isPhoneBlind()
        );

        return member;
    }

    @Transactional
    public void deleteUserProfileLink(Long linkId, Long memberId) {
        MemberLink link = memberLinkRepository.findByIdAndMemberId(linkId, memberId)
                .orElseThrow(() -> new NotFoundDBEntityException("Member Profile Link"));
        memberLinkRepository.delete(link);
    }

    @Transactional
    public void deleteUserProfileActivity(Long activityId, Long memberId) {
        MemberSoptActivity activity = memberSoptActivityRepository.findByIdAndMemberId(activityId, memberId)
                .orElseThrow(() -> new NotFoundDBEntityException("Member Profile Activity"));
        memberSoptActivityRepository.delete(activity);
    }

//    @Transactional(readOnly = true)
//    public List<Member> getMemberBySearchCond(String search) {
//        return memberProfileQueryRepository.findAllMemberProfilesBySearchCond(search);
//    }

    @Transactional
    public void checkActivities(Long memberId, Boolean isCheck) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundDBEntityException("Member"));

        member.editActivityChange(isCheck);
    }

    @Transactional(readOnly = true)
    public MemberBlockResponse getBlockStatus(Long memberId, Long blockedMemberId) {
        Member blocker = memberRetriever.findMemberById(memberId);
        Member blockedMember = memberRetriever.findMemberById(blockedMemberId);
        InternalUserDetails blockerDetail = platformService.getInternalUser(memberId);
        InternalUserDetails blockedMemberDetail = platformService.getInternalUser(blockedMemberId);

        Optional<MemberBlock> blockHistory = memberBlockRepository.findByBlockerAndBlockedMember(blocker, blockedMember);
        return blockHistory.map(memberBlock ->
                MemberBlockResponse.of(memberBlock.getIsBlocked(), blockerDetail, blockedMemberDetail)
        ).orElseGet(() -> MemberBlockResponse.of(false, blockerDetail, blockedMemberDetail));
    }

    @Transactional
    public void blockUser(Long memberId, Long blockedMemberId) {
        Member blocker = memberRetriever.findMemberById(memberId);
        Member blockedMember = memberRetriever.findMemberById(blockedMemberId);

        Optional<MemberBlock> blockHistory = memberBlockRepository.findByBlockerAndBlockedMember(blocker, blockedMember);
        if (blockHistory.isPresent()) {
            MemberBlock block = blockHistory.get();
            block.updateIsBlocked(true);
            memberBlockRepository.save(block);
        } else {
            MemberBlock newBlock = MemberBlock.builder()
                    .blocker(blocker)
                    .blockedMember(blockedMember).build();
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

        memberReportRepository.save(MemberReport.builder()
                .reporter(reporter)
                .reportedMember(reportedMember).build()
        );
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

        return memberResponseMapper.toMemberPropertiesResponse(
                member, memberCareer, coffeeChatProperty,
                activitiesAndGeneration, uploadSopticleCount, uploadReviewCount);
    }

    private void sendReportToSlack(InternalUserDetails reporter, InternalUserDetails reportedMember) {
        try {
            if (Objects.equals(activeProfile, "prod")) {
                val slackRequest = createReportSlackRequest(reporter.userId(), reporter.name(), reportedMember.userId(), reportedMember.name());
                slackClient.postReportMessage(slackRequest.toString());
            }
        } catch (RuntimeException ex) {
            log.error("슬랙 요청이 실패했습니다 : " + ex.getMessage());
        }
    }

    private JsonNode createReportSlackRequest(Long blockerId, String blockerName, Long blockedMemberId, String blockedMemberName) {
        val rootNode = slackMessageUtil.getObjectNode();
        rootNode.put("text", "🚨유저 신고 발생!🚨");

        val blocks = slackMessageUtil.getArrayNode();
        val textField = slackMessageUtil.createTextField("유저 신고가 들어왔어요!");
        val contentNode = slackMessageUtil.createSection();

        val fields = slackMessageUtil.getArrayNode();
        fields.add(slackMessageUtil.createTextFieldNode("*신고자:*\n" + blockerName + "(id: " + blockerId + ")"));
        fields.add(slackMessageUtil.createTextFieldNode("*신고 당한 유저:*\n" + blockedMemberName + "(id: " + blockedMemberId + ")" + "<https://playground.sopt.org/members/" + blockedMemberId + ">"));
        contentNode.set("fields", fields);

        blocks.add(textField);
        blocks.add(contentNode);
        rootNode.set("blocks", blocks);
        return rootNode;
    }

    private String checkTeamNullCondition (String team) {
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
                .collect(Collectors.toMap(Member::getId, Member::getCareers, (a,b) -> a));

        return userDetails.stream()
                .map(userDetail -> {
                    List<MemberSoptActivityResponse> memberSoptActivityResponses = userDetail.soptActivities().stream()
                            .map(activity -> new MemberSoptActivityResponse(
                                    (long)activity.activityId(),
                                    activity.generation()))
                            .toList();
                    List<MemberCareer> memberCareers = careerMap.getOrDefault(userDetail.userId(), Collections.emptyList());
                    List<MemberCareerResponse> memberCareerResponses = memberCareers.stream()
                            .map(memberCareer -> new MemberCareerResponse(
                                    memberCareer.getId(),
                                    memberCareer.getCompanyName(),
                                    memberCareer.getTitle(),
                                    memberCareer.getIsCurrent()))
                            .toList();

                    return new MakersMemberProfileResponse(userDetail.userId(), userDetail.name(), userDetail.profileImage(), memberSoptActivityResponses, memberCareerResponses);
                }).toList();
    }
}
