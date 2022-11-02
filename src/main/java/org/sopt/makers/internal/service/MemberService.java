package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberLink;
import org.sopt.makers.internal.domain.MemberSoptActivity;
import org.sopt.makers.internal.dto.member.MemberProfileSaveRequest;
import org.sopt.makers.internal.dto.member.MemberProfileUpdateRequest;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.MemberLinkRepository;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.repository.MemberSoptActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberLinkRepository memberLinkRepository;
    private final MemberSoptActivityRepository memberSoptActivityRepository;

    @Transactional(readOnly = true)
    public Member getMemberById (Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new NotFoundDBEntityException("Member"));
    }

    @Transactional(readOnly = true)
    public Member getMemberHasProfileById (Long id) {
        return memberRepository.findByIdAndHasProfileTrue(id).orElseThrow(() -> new NotFoundDBEntityException("Member without profile"));
    }

    @Transactional(readOnly = true)
    public List<Member> getMemberProfiles() {
        return memberRepository.findAllByHasProfileTrue();
    }

    @Transactional
    public Member saveMemberProfile (Long id, MemberProfileSaveRequest request) {
        val member = memberRepository.findById(id).orElseThrow(() -> new NotFoundDBEntityException("Member"));
        val memberLinks = memberLinkRepository.saveAll(
                request.links().stream().map(link ->
                        MemberLink.builder()
                                .memberId(id)
                                .title(link.title())
                                .url(link.url())
                                .build()).collect(Collectors.toList())
        );

        val memberActivities = memberSoptActivityRepository.saveAll(
                request.activities().stream().map(activity ->
                        MemberSoptActivity.builder()
                                .memberId(id)
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
    public Member updateMemberProfile (Long id, MemberProfileUpdateRequest request) {
        val member = getMemberById(id);
        val memberLinks = memberLinkRepository.saveAll(
                request.links().stream().map(link ->
                        MemberLink.builder()
                                .id(link.id())
                                .memberId(id)
                                .title(link.title())
                                .url(link.url())
                                .build()).collect(Collectors.toList())
        );

        val memberActivities = memberSoptActivityRepository.saveAll(
                request.activities().stream().map(activity ->
                        MemberSoptActivity.builder()
                                .id(activity.id())
                                .memberId(id)
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
    public Member getMemberByName (String name) {
        return memberRepository.findByName(name).orElseThrow(() -> new NotFoundDBEntityException("Member"));
    }

}
