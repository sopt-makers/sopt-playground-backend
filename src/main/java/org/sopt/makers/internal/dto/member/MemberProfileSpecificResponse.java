package org.sopt.makers.internal.dto.member;

import java.time.LocalDate;
import java.util.List;

public record MemberProfileSpecificResponse(
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
        List<MemberActivityResponse> activities,
        List<MemberLinkResponse> links,
        List<MemberProjectResponse> projects,
        List<MemberCareerResponse> careers,
        Boolean allowOfficial,
        Boolean isMine
) {

    public record MemberLinkResponse(
            Long id,
            String title,
            String url
    ){}

    public record MemberProjectResponse(
            Long id,
            String name,
            String summary,
            Integer generation,
            String category,
            String logoImage,
            String thumbnailImage,
            String[] serviceType
    ){}

    public record MemberActivityResponse (
            String cardinalInfo,
            List<ActivityVo> cardinalActivities
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
