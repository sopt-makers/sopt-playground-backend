package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberLink;
import org.sopt.makers.internal.domain.MemberSoptActivity;
import org.sopt.makers.internal.dto.member.*;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.mapper.MemberMapper;
import org.sopt.makers.internal.repository.MemberLinkRepository;
import org.sopt.makers.internal.repository.MemberProfileQueryRepository;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.repository.MemberSoptActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final MemberProfileQueryRepository memberProfileQueryRepository;
    private final MemberMapper memberMapper;

    private final MemberProfileQueryRepository profileQueryRepository;

    @Transactional(readOnly = true)
    public Member getMemberById (Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new NotFoundDBEntityException("Member"));
    }

    @Transactional(readOnly = true)
    public Member getMemberHasProfileById (Long id) {
        return memberRepository.findByIdAndHasProfileTrue(id).orElseThrow(() -> new NotFoundDBEntityException("Member without profile"));
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
    public List<Member> getMemberProfiles(Integer filter, Integer limit, Integer cursor) {
        val part = getMemberPart(filter);
        if (part != null && cursor != null && limit != null)
            return memberProfileQueryRepository.findAllLimitedMemberProfileByPart(part, limit, cursor);
        if (part != null)
            return memberProfileQueryRepository.findAllMemberProfileByPart(part);
        if (limit != null && cursor != null)
            return memberProfileQueryRepository.findAllLimitedMemberProfile(limit, cursor);
        return memberRepository.findAllByHasProfileTrue();
    }

    private String getMemberPart (int filter) {
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
        member.saveMemberProfile(
                request.name(), request.profileImage(), request.birthday(), request.phone(), request.email(),
                request.address(), request.university(), request.major(), request.introduction(), request.skill(),
                request.openToWork(), request.openToSideProject(), request.allowOfficial(),
                memberActivities, memberLinks
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
        member.saveMemberProfile(
                request.name(), request.profileImage(), request.birthday(), request.phone(), request.email(),
                request.address(), request.university(), request.major(), request.introduction(), request.skill(),
                request.openToWork(), request.openToSideProject(), request.allowOfficial(),
                memberActivities, memberLinks
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
