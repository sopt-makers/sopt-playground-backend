package org.sopt.makers.internal.community.dto;

import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberCareer;
import org.sopt.makers.internal.member.domain.MemberSoptActivity;

import java.util.Comparator;

public record MemberVo(
        Long id,
        String name,
        String profileImage,
        SoptActivityVo activity,
        CareerVo careers
) {
    public static MemberVo of(InternalUserDetails userDetails, MemberCareer career) {
        if (userDetails == null) return null;

        SoptActivity latestSoptActivity = userDetails.soptActivities() == null ? null : userDetails.soptActivities().stream()
                .max(Comparator.comparing(SoptActivity::generation))
                .orElse(null);

        SoptActivityVo activityVo = latestSoptActivity != null
                ? new SoptActivityVo(latestSoptActivity.generation(), latestSoptActivity.part(), latestSoptActivity.team())
                : null;

        CareerVo careerVo = career != null
                ? new CareerVo(career.getCompanyName(), career.getTitle())
                : null;

        return new MemberVo(
                userDetails.userId(),
                userDetails.name(),
                userDetails.profileImage(),
                activityVo,
                careerVo
        );
    }

    /**
     * [LEGACY] 기존 Member 엔티티로 MemberVo를 생성
     */
//    public static MemberVo of(Member member) {
//        if (member == null) return null;
//
//        MemberCareer career = (member.getCareers() == null || member.getCareers().isEmpty()) ?
//                null : member.getCareers().stream().filter(MemberCareer::getIsCurrent).findFirst().orElse(null);
//
//        MemberSoptActivity activity = member.getActivities() == null ? null : member.getActivities().stream()
//                .max(Comparator.comparingInt(MemberSoptActivity::getGeneration))
//                .orElse(null);
//
//        SoptActivityVo activityVo = activity != null
//                ? new SoptActivityVo(activity.getGeneration(), activity.getPart(), activity.getTeam())
//                : null;
//
//        CareerVo careerVo = career != null
//                ? new CareerVo(career.getCompanyName(), career.getTitle())
//                : null;
//
//        return new MemberVo(
//                member.getId(),
//                member.getName(),
//                member.getProfileImage(),
//                activityVo,
//                careerVo
//        );
//    }
}