package org.sopt.makers.internal.dto.member;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record MemberProfileSpecificResponse(
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
        List<MemberActivityResponse> activities,
        List<MemberLinkResponse> links,
        List<MemberProjectResponse> projects,
        List<MemberCareerResponse> careers,
        Boolean openToWork,
        Boolean openToSideProject,
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
            String title,
            String startDate,
            String endDate,
            Boolean isCurrent
    ){}
}
