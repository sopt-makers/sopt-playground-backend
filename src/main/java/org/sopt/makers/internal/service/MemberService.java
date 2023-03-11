package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.domain.MemberLink;
import org.sopt.makers.internal.domain.MemberSoptActivity;
import org.sopt.makers.internal.dto.member.*;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.MemberHasNotProfileException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.mapper.MemberMapper;
import org.sopt.makers.internal.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberLinkRepository memberLinkRepository;
    private final MemberSoptActivityRepository memberSoptActivityRepository;
    private final MemberCareerRepository memberCareerRepository;
    private final MemberProfileQueryRepository memberProfileQueryRepository;
    private final MemberMapper memberMapper;

    private final MemberProfileQueryRepository profileQueryRepository;

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
        return profileQueryRepository.findMemberProfileProjectsByMemberId(id);
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

    @Transactional(readOnly = true)
    public List<Member> getAllMakersMemberProfiles() {
        val makersMembers = List.of(
                1L, 44L, 23L, 31L, 8L, 30L, 40L, 46L, 26L, 60L, 39L, 6L, 9L, 7L, 2L, 3L, 29L,
                5L, 38L, 37L, 13L, 28L, 36L, 58L, 173L, 32L, 43L, 188L, 59L, 34L, 21L, 33L, 22L, 35L, 45L,
                186L, 227L, 264L, 4L, 51L, 187L, 128L, 64L, 99L, 10L, 66L, 260L, 72L, 265L, 74L, 251L,
                115L, 258L, 112L, 205L, 238L
        );
        return memberRepository.findAllByHasProfileTrueAndIdIn(makersMembers);
    }

    @Transactional(readOnly = true)
    public List<Member> getMemberProfiles(Integer filter, Integer limit, Integer cursor, String name) {
        val part = getMemberPart(filter);
        if (name == null) {
            if (part != null && cursor != null && limit != null)
                return memberProfileQueryRepository.findAllLimitedMemberProfileByPart(part, limit, cursor);
            if (part != null)
                return memberProfileQueryRepository.findAllMemberProfileByPart(part);
            if (limit != null && cursor != null)
                return memberProfileQueryRepository.findAllLimitedMemberProfile(limit, cursor);
        } else {
            if (part != null && cursor != null && limit != null)
                return memberProfileQueryRepository.findAllLimitedMemberProfileByPartAndName(part, limit, cursor, name);
            if (part != null)
                return memberProfileQueryRepository.findAllMemberProfileByPartAndName(part, name);
            if (limit != null && cursor != null)
                return memberProfileQueryRepository.findAllLimitedMemberProfileByName(limit, cursor, name);
            return memberProfileQueryRepository.findAllByName(name);
        }
        return memberRepository.findAllByHasProfileTrue();
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
                                .team(activity.team())
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
        member.saveMemberProfile(
                request.name(), request.profileImage(), request.birthday(), request.phone(), request.email(),
                request.address(), request.university(), request.major(), request.introduction(),
                request.skill(), request.mbti(), request.mbtiDescription(), request.sojuCapacity(),
                request.interest(), request.isPourSauceLover(), request.isHardPeachLover(), request.isMintChocoLover(),
                request.isRedBeanLover(), request.isSojuLover(), request.isRiceTteokLover(), request.idealType(),
                request.selfIntroduction(), request.allowOfficial(),
                memberActivities, memberLinks, memberCareers
        );
        return member;
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
                                .team(activity.team())
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

        member.saveMemberProfile(
                request.name(), request.profileImage(), request.birthday(), request.phone(), request.email(),
                request.address(), request.university(), request.major(), request.introduction(),
                request.skill(), request.mbti(), request.mbtiDescription(), request.sojuCapacity(),
                request.interest(), request.isPourSauceLover(), request.isHardPeachLover(), request.isMintChocoLover(),
                request.isRedBeanLover(), request.isSojuLover(), request.isRiceTteokLover(), request.idealType(),
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
