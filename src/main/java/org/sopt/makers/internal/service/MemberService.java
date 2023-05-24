package org.sopt.makers.internal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.common.MakersMemberId;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.domain.MemberLink;
import org.sopt.makers.internal.domain.MemberSoptActivity;
import org.sopt.makers.internal.domain.UserFavor;
import org.sopt.makers.internal.dto.member.*;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.MemberHasNotProfileException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.external.SlackClient;
import org.sopt.makers.internal.mapper.MemberMapper;
import org.sopt.makers.internal.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {
    @Value("${spring.profiles.active}")
    private String activeProfile;
    private final MemberRepository memberRepository;
    private final MemberLinkRepository memberLinkRepository;
    private final MemberSoptActivityRepository memberSoptActivityRepository;
    private final MemberCareerRepository memberCareerRepository;
    private final MemberProfileQueryRepository memberProfileQueryRepository;
    private final MemberMapper memberMapper;
    private final SlackClient slackClient;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public Member getMemberById (Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new NotFoundDBEntityException("Member"));
    }

    @Transactional(readOnly = true)
    public Member getMemberHasProfileById (Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundDBEntityException("해당 id의 Member를 찾을 수 없습니다."));
        if (member.getHasProfile()) return member;
        else throw new MemberHasNotProfileException("해당 Member는 프로필이 없습니다.");
    }

    @Transactional(readOnly = true)
    public List<MemberProfileProjectDao> getMemberProfileProjects (Long id) {
        return memberProfileQueryRepository.findMemberProfileProjectsByMemberId(id);
    }

    public Map<String, List<ActivityVo>> getMemberProfileActivity (
            List<MemberSoptActivity> memberActivities,
            List<MemberProfileProjectDao> memberProfileProjects
    ) {
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

    public  List<MemberProfileProjectVo> getMemberProfileProjects (
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
    public int getMemberProfilesCount(Integer filter, String name, Integer generation,
           Double sojuCapacity, String mbti, String team) {
        val part = getMemberPart(filter);
        return memberProfileQueryRepository.countAllMemberProfile(part, name, generation, sojuCapacity, mbti, team);
    }

    @Transactional(readOnly = true)
    public List<Member> getMemberProfiles(Integer filter, Integer limit, Integer cursor, String name, Integer generation,
                                          Double sojuCapacity, Integer orderBy, String mbti, String team) {
        val part = getMemberPart(filter);
        if(limit != null) {
            return memberProfileQueryRepository.findAllLimitedMemberProfile(part, limit, cursor, name, generation,
                    sojuCapacity, orderBy, mbti, team);
        }
        else {
            return memberProfileQueryRepository.findAllMemberProfile(part, cursor, name, generation,
                    sojuCapacity, orderBy, mbti, team);
        }
    }

    private String getMemberPart (Integer filter) {
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

    private String checkActivityTeamConditions (String team) {
        Predicate<String> teamIsEmpty = Objects::isNull;
        Predicate<String> teamIsNullString = s -> s.equals("해당 없음");
        val isNullResult = teamIsEmpty.or(teamIsNullString).test(team);
        if(isNullResult) return null;
        else return team;
    }

    @Transactional
    public Member saveMemberProfile (Long id, MemberProfileSaveRequest request) {
        val member = memberRepository.findById(id).orElseThrow(() -> new NotFoundDBEntityException("Member"));
        val memberId = member.getId();
        if (memberId == null) throw new NotFoundDBEntityException("Member id is null");
        val memberLinkEntities = request.links().stream().map(link ->
                        MemberLink.builder()
                                .memberId(memberId)
                                .title(link.title())
                                .url(link.url())
                                .build()).collect(Collectors.toList());
        memberLinkEntities.forEach(link -> link.setMemberId(memberId));
        val memberLinks = memberLinkRepository.saveAll(memberLinkEntities);
        
        val memberActivityEntities = request.activities().stream().map(activity ->
                        MemberSoptActivity.builder()
                                .memberId(memberId)
                                .part(activity.part())
                                .generation(activity.generation())
                                .team(checkActivityTeamConditions(activity.team()))
                                .build()).collect(Collectors.toList());
        memberActivityEntities.forEach(a -> a.setMemberId(memberId));
        val nnActivities = memberActivityEntities.stream().filter(l -> l.getMemberId() != null).count();
        if (nnActivities == 0) throw new NotFoundDBEntityException("There's no activities with memberId");
        val memberActivities = memberSoptActivityRepository.saveAll(memberActivityEntities);

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
                memberActivities, memberLinks, memberCareers
        );
        try {
            if (Objects.equals(activeProfile, "prod")) {
                val slackRequest = createSlackRequest(member.getId(), request.name(), request.idealType());
                slackClient.postMessage(slackRequest.toString());
            }
        } catch (RuntimeException ex) {
            log.error("슬랙 요청이 실패했습니다 : " + ex.getMessage());
        }
        return member;
    }

    private JsonNode createSlackRequest(Long id, String name, String idealType) {
        val rootNode = jsonMapper.createObjectNode();
        rootNode.put("text", "새로운 유저가 프로필을 만들었어요!");
        val blocks = jsonMapper.createArrayNode();

        val textField = jsonMapper.createObjectNode();
        textField.put("type", "section");
        textField.set("text", createTextFieldNode("새로운 유저가 프로필을 만들었어요!"));

        val contentNode = jsonMapper.createObjectNode();
        contentNode.put("type", "section");
        val fields = jsonMapper.createArrayNode();
        fields.add(createTextFieldNode("*이름:*\n" + name));
        fields.add(createTextFieldNode("*프로필링크:*\n<https://playground.sopt.org/members/" + id + "|멤버프로필>"));
        fields.add(createTextFieldNode("*이상형:*\n" + idealType));
        contentNode.set("fields", fields);

        blocks.add(textField);
        blocks.add(contentNode);
        rootNode.set("blocks", blocks);
        return rootNode;
    }

    private JsonNode createTextFieldNode (String text) {
        val textField = jsonMapper.createObjectNode();
        textField.put("type", "mrkdwn");
        textField.put("text", text);
        return textField;
    }

    @Transactional
    public Member updateMemberProfile (Long id, MemberProfileUpdateRequest request) {
        val member = getMemberById(id);
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
                                .id(activity.id())
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
                request.name(), request.profileImage(), request.birthday(), request.phone(), request.email(),
                request.address(), request.university(), request.major(), request.introduction(),
                request.skill(), request.mbti(), request.mbtiDescription(), request.sojuCapacity(),
                request.interest(), userFavor, request.idealType(),
                request.selfIntroduction(), request.allowOfficial(),
                memberActivities, memberLinks, memberCareers
        );
        return member;
    }

    @Transactional
    public void deleteUserProfileLink (Long linkId, Long memberId) {
        val link = memberLinkRepository.findByIdAndMemberId(linkId, memberId)
                .orElseThrow(() -> new NotFoundDBEntityException("Member Profile Link"));
        memberLinkRepository.delete(link);
    }

    @Transactional
    public void deleteUserProfileActivity (Long activityId, Long memberId) {
        val activity = memberSoptActivityRepository.findByIdAndMemberId(activityId, memberId)
                .orElseThrow(() -> new NotFoundDBEntityException("Member Profile Activity"));
        memberSoptActivityRepository.delete(activity);
    }

    @Transactional(readOnly = true)
    public List<Member> getMemberByName (String name) {
        return memberRepository.findAllByNameContaining(name);
    }
}
