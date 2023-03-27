package org.sopt.makers.internal.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record MemberProfileSpecificResponse(
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
        String interest,
        @Schema(required = true)
        List<MemberActivityResponse> activities,
        List<MemberLinkResponse> links,
        List<MemberProjectResponse> projects,
        List<MemberCareerResponse> careers,
        Boolean allowOfficial,
        @Schema(required = true)
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
