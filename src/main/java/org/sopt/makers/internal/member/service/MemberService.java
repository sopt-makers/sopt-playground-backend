package org.sopt.makers.internal.member.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.auth.AuthConfig;
import org.sopt.makers.internal.common.util.InfiniteScrollUtil;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformClient;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.member.domain.MakersMemberId;
import org.sopt.makers.internal.external.slack.SlackMessageUtil;
import org.sopt.makers.internal.community.repository.post.CommunityPostRepository;
import org.sopt.makers.internal.community.service.ReviewService;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberCareer;
import org.sopt.makers.internal.member.domain.MemberLink;
import org.sopt.makers.internal.member.domain.MemberSoptActivity;
import org.sopt.makers.internal.member.domain.UserFavor;
import org.sopt.makers.internal.member.domain.MemberBlock;
import org.sopt.makers.internal.member.domain.MemberReport;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.MemberHasNotProfileException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.external.slack.SlackClient;
import org.sopt.makers.internal.member.dto.ActivityVo;
import org.sopt.makers.internal.member.dto.MemberProfileProjectDao;
import org.sopt.makers.internal.member.dto.MemberProfileProjectVo;
import org.sopt.makers.internal.member.dto.request.MemberProfileSaveRequest;
import org.sopt.makers.internal.member.dto.request.MemberProfileUpdateRequest;
import org.sopt.makers.internal.member.dto.response.MemberAllProfileResponse;
import org.sopt.makers.internal.member.dto.response.MemberBlockResponse;
import org.sopt.makers.internal.member.dto.response.MemberInfoResponse;
import org.sopt.makers.internal.member.dto.response.MemberProfileResponse;
import org.sopt.makers.internal.member.dto.response.MemberProfileSpecificResponse;
import org.sopt.makers.internal.member.dto.response.MemberProfileSpecificResponse.MemberActivityResponse;
import org.sopt.makers.internal.member.dto.response.MemberResponse;
import org.sopt.makers.internal.member.mapper.MemberMapper;
import org.sopt.makers.internal.member.dto.response.MemberPropertiesResponse;
import org.sopt.makers.internal.member.mapper.MemberResponseMapper;
import org.sopt.makers.internal.member.repository.MemberLinkRepository;
import org.sopt.makers.internal.member.repository.MemberProfileQueryRepository;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.sopt.makers.internal.member.repository.career.MemberCareerRepository;
import org.sopt.makers.internal.coffeechat.dto.request.InternalCoffeeChatMemberDto;
import org.sopt.makers.internal.member.repository.soptactivity.MemberSoptActivityRepository;
import org.sopt.makers.internal.member.service.career.MemberCareerRetriever;
import org.sopt.makers.internal.coffeechat.service.CoffeeChatRetriever;
import org.sopt.makers.internal.coffeechat.dto.request.MemberCoffeeChatPropertyDto;
import org.sopt.makers.internal.member.repository.MemberBlockRepository;
import org.sopt.makers.internal.member.repository.MemberReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRetriever memberRetriever;
    private final CoffeeChatRetriever coffeeChatRetriever;
    private final MemberCareerRetriever memberCareerRetriever;
    private final MemberResponseMapper memberResponseMapper;
    @Value("${spring.profiles.active}")
    private String activeProfile;
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
    private final AuthConfig authConfig;
    private final PlatformClient platformClient;
    private final InfiniteScrollUtil infiniteScrollUtil;

    public InternalUserDetails getInternalUserById(Long id) {
        return platformClient.getInternalUserDetails(authConfig.getPlatformApiKey(),
                authConfig.getPlatformServiceName(), new ArrayList<>(List.of(id))).getBody().getData().get(0);
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse getMyInformation(Long userId) {
        Member member = getMemberById(userId);
        InternalUserDetails userDetails = platformService.getInternalUser(userId);
        boolean isCoffeeChatActive = coffeeChatRetriever.existsCoffeeChat(member);
        return memberResponseMapper.toMemberInfoResponse(member, userDetails, isCoffeeChatActive);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getMemberByName(String name) {
        List<Member> members = memberRepository.findAllByNameContaining(name);
        if (members.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = members.stream().map(Member::getId).collect(Collectors.toList());
        Map<Long, InternalUserDetails> userDetailsMap = platformService.getInternalUsers(userIds).stream()
                .collect(Collectors.toMap(InternalUserDetails::userId, Function.identity()));

        return members.stream().map(member -> {
            InternalUserDetails userDetails = userDetailsMap.get(member.getId());
            if (userDetails == null) return null;
            return new MemberResponse(
                    member.getId(),
                    userDetails.name(),
                    userDetails.lastGeneration(),
                    userDetails.profileImage(),
                    member.getHasProfile(),
                    member.getEditActivitiesAble()
            );
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

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
                member, isMine, memberProfileProjects, activityResponses, soptActivityResponse, isCoffeeChatActivate
        );

        MemberProfileSpecificResponse finalResponse = new MemberProfileSpecificResponse(
                userDetails.name(), userDetails.profileImage(), response.birthday(), response.isPhoneBlind(),
                response.phone(), response.email(), response.address(), response.university(), response.major(),
                response.introduction(), response.skill(), response.mbti(), response.mbtiDescription(),
                response.sojuCapacity(), response.interest(), response.userFavor(), response.idealType(),
                response.selfIntroduction(), response.activities(), response.soptActivities(), response.links(),
                response.projects(), response.careers(), response.allowOfficial(), response.isCoffeeChatActivate(), response.isMine()
        );

        return MemberProfileSpecificResponse.applyPhoneMasking(finalResponse, isMine, isCoffeeChatActivate);
    }

    @Transactional(readOnly = true)
    public Member getMemberById(Long id) {
        return memberRetriever.findMemberById(id);
    }

    public MemberResponse getMemberResponseById(Long id) {
        InternalUserDetails user = getInternalUserById(id);
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
    public List<Member> getAllMakersMemberProfiles() {
        return memberRepository.findAllByHasProfileTrueAndIdIn(MakersMemberId.getMakersMember());
    }

    @Transactional(readOnly = true)
    public MemberAllProfileResponse getMemberProfiles(
            Integer filter, Integer limit, Integer cursor, String search,
            Integer generation, Integer employed, Integer orderBy, String mbti, String team
    ) {
        List<Member> members = memberProfileQueryRepository.findAllLimitedMemberProfile(
                    getMemberPart(filter), infiniteScrollUtil.checkLimitForPagination(limit),
                    cursor, search, generation, employed, orderBy, mbti, team);

        if (members.isEmpty()) {
            return new MemberAllProfileResponse(Collections.emptyList(), false, 0);
        }

        List<Long> userIds = members.stream().map(Member::getId).collect(Collectors.toList());
        Map<Long, InternalUserDetails> userDetailsMap = platformService.getInternalUsers(userIds).stream()
                .collect(Collectors.toMap(InternalUserDetails::userId, Function.identity()));
        List<MemberProfileResponse> memberList = members.stream().map(member -> {
            InternalUserDetails userDetails = userDetailsMap.get(member.getId());
            if (Objects.isNull(userDetails)) {
                return null;
            }
            boolean isCoffeeChatActivate = coffeeChatRetriever.existsCoffeeChat(member);
//            MemberProfileResponse profileResponse = memberMapper.toProfileResponse(member, isCoffeeChatActivate);

            return MemberProfileResponse.checkIsBlindPhone(
                    new MemberProfileResponse(
                            member.getId(), userDetails.name(), userDetails.profileImage(), userDetails.birthday(),
                            userDetails.phone(), userDetails.email(), member.getAddress(), member.getUniversity(),
                            member.getMajor(), member.getIntroduction(), member.getSkill(), member.getMbti(),
                            member.getMbtiDescription(), member.getSojuCapacity(), member.getInterest(),
                            memberMapper.toProfileResponse(member, isCoffeeChatActivate).userFavor(),
                            member.getIdealType(), member.getSelfIntroduction(),
                            memberMapper.toProfileResponse(member, isCoffeeChatActivate).activities(),
                            memberMapper.toProfileResponse(member, isCoffeeChatActivate).links(),
                            memberMapper.toProfileResponse(member, isCoffeeChatActivate).careers(),
                            member.getAllowOfficial(), isCoffeeChatActivate
                    ),
                    memberMapper.mapPhoneIfBlind(member.getIsPhoneBlind(), member.getPhone())
            );
        }).filter(Objects::nonNull).collect(Collectors.toList());

        boolean hasNextMember = infiniteScrollUtil.checkHasNextElement(limit, memberList);
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

    @Transactional(readOnly = true)
    public List<InternalCoffeeChatMemberDto> getAllMemberByCoffeeChatActivate() {
        List<Member> members = memberRetriever.findAllMembersByCoffeeChatActivate();
        return members.stream().map(member -> InternalCoffeeChatMemberDto.of(
                member, platformService.getPartAndGenerationList(member.getId())
        )).toList();
    }

    @Transactional
    public Member saveMemberProfile(Long userId, MemberProfileSaveRequest request) {
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
                request.name(), request.profileImage(), request.birthday(), request.phone(), request.email(),
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
                member.getName(), request.profileImage(), request.birthday(), request.phone(), request.email(),
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
        val activity = memberSoptActivityRepository.findByIdAndMemberId(activityId, memberId)
                .orElseThrow(() -> new NotFoundDBEntityException("Member Profile Activity"));
        memberSoptActivityRepository.delete(activity);
    }

    @Transactional(readOnly = true)
    public List<Member> getMemberBySearchCond(String search) {
        return memberProfileQueryRepository.findAllMemberProfilesBySearchCond(search);
    }

    @Transactional
    public void checkActivities(Long memberId, Boolean isCheck) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundDBEntityException("Member"));

        member.editActivityChange(isCheck);
    }

    @Transactional(readOnly = true)
    public MemberBlockResponse getBlockStatus(Long memberId, Long blockedMemberId) {
        val blocker = memberRetriever.findMemberById(memberId);
        val blockedMember = memberRetriever.findMemberById(blockedMemberId);

        val blockHistory = memberBlockRepository.findByBlockerAndBlockedMember(blocker, blockedMember);
        return blockHistory.map(memberBlock ->
                MemberBlockResponse.of(memberBlock.getIsBlocked(), blocker, blockedMember)
        ).orElseGet(() -> MemberBlockResponse.of(false, blocker, blockedMember));
    }

    @Transactional
    public void blockUser(Long memberId, Long blockedMemberId) {
        val blocker = memberRetriever.findMemberById(memberId);
        val blockedMember = memberRetriever.findMemberById(blockedMemberId);

        val blockHistory = memberBlockRepository.findByBlockerAndBlockedMember(blocker, blockedMember);
        if (blockHistory.isPresent()) {
            val block = blockHistory.get();
            block.updateIsBlocked(true);
            memberBlockRepository.save(block);
        } else {
            val newBlock = MemberBlock.builder()
                    .blocker(blocker)
                    .blockedMember(blockedMember).build();
            memberBlockRepository.save(newBlock);
        }
    }

    @Transactional
    public void reportUser(Long memberId, Long reportedMemberId) {
        val reporter = memberRetriever.findMemberById(memberId);
        val reportedMember = memberRetriever.findMemberById(reportedMemberId);

        sendReportToSlack(reporter, reportedMember);

        memberReportRepository.save(MemberReport.builder()
                .reporter(reporter)
                .reportedMember(reportedMember).build()
        );
    }

    @Transactional(readOnly = true)
    public MemberPropertiesResponse getMemberProperties(Long memberId) {
        Member member = memberRetriever.findMemberById(memberId);
        MemberCareer memberCareer = memberCareerRetriever.findMemberLastCareerByMemberId(memberId);
        MemberCoffeeChatPropertyDto coffeeChatProperty = coffeeChatRetriever.getMemberCoffeeChatProperty(member);
        List<String> activitiesAndGeneration = platformService.getPartAndGenerationList(memberId);

        long uploadSopticleCount = communityPostRepository.countSopticleByMemberId(memberId);
        long uploadReviewCount = reviewService.fetchReviewCountByUsername(member.getName());

        return memberResponseMapper.toMemberPropertiesResponse(
                member, memberCareer, coffeeChatProperty,
                activitiesAndGeneration, uploadSopticleCount, uploadReviewCount);
    }

    private void sendReportToSlack(Member reporter, Member reportedMember) {
        try {
            if (Objects.equals(activeProfile, "prod")) {
                val slackRequest = createReportSlackRequest(reporter.getId(), reporter.getName(), reportedMember.getId(), reportedMember.getName());
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
}
