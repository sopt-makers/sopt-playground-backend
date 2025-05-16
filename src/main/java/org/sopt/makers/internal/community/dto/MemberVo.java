package org.sopt.makers.internal.community.dto;

import lombok.val;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberCareer;
import org.sopt.makers.internal.member.domain.MemberSoptActivity;

import java.util.Comparator;

public record MemberVo(
        Long id,
        String name,
        String profileImage,
        MemberSoptActivity activity,
        MemberCareer careers
) {
    public static MemberVo of(Member member) {
        MemberCareer career = (member.getCareers() == null || member.getCareers().stream().noneMatch(MemberCareer::getIsCurrent)) ?
                null : member.getCareers().stream().filter(MemberCareer::getIsCurrent).toList().get(0);
        MemberSoptActivity activity = member.getActivities().stream()
                .max(Comparator.comparingInt(MemberSoptActivity::getGeneration))
                .orElse(null);

        return new MemberVo(
                member.getId(),
                member.getName(),
                member.getProfileImage(),
                activity,
                career
        );
    }
}