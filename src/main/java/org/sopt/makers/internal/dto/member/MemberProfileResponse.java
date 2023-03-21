package org.sopt.makers.internal.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record MemberProfileResponse(
        @Schema(required = true)
        Long id,
        @Schema(required = true)
        String name,
        String profileImage,
        LocalDate birthday,
        String phone,
        String email,
        String address,
        String university,
        String major,
        String introduction,
        String skill,
        String mbti,
        String mbtiDescription,
        Double sojuCapacity,
        String interest,
        Boolean isPourSauceLover,
        Boolean isHardPeachLover,
        Boolean isMintChocoLover,
        Boolean isRedBeanFishBreadLover,
        Boolean isSojuLover,
        Boolean isRiceTteokLover,
        String idealType,
        String selfIntroduction,
        @Schema(required = true)
        List<MemberSoptActivityResponse> activities,
        List<MemberLinkResponse> links,
        List<MemberCareerResponse> careers,
        Boolean allowOfficial
) {

    public record MemberLinkResponse(
            Long id,
            String title,
            String url
    ){}

    public record MemberSoptActivityResponse(
            Long id,
            Integer generation,
            String part,
            String team
    ){}

    public record MemberCareerResponse(
            Long id,
            String companyName,
            String title,
            String startDate,
            String endDate,
            Boolean isCurrent
    ){}
}
