package org.sopt.makers.internal.dto.member;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public record MemberProfileResponse(
        Long id,
        String name,
        String profileImage,
        LocalDate birthday,
        String phone,
        String email,
        String address,
        String university,
        String major,
        String keyIntroduction,
        String skill,
        String mbti,
        String personality,
        Integer sojuCapacity,
        String interestedIn,
        Boolean pourSauce,
        Boolean hardPeach,
        Boolean mintChoco,
        Boolean redBeanFish,
        Boolean soju,
        Boolean riceTteok,
        String idealType,
        String selfIntroduction,
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
