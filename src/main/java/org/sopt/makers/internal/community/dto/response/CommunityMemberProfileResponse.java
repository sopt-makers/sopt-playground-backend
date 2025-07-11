package org.sopt.makers.internal.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record CommunityMemberProfileResponse(
        @Schema(required = true)
        Long id,
        @Schema(required = true)
        String name,
        String profileImage,
        @Schema(required = true)
        List<MemberSoptActivityResponse> activities,
        List<MemberLinkResponse> links,
        List<MemberCareerResponse> careers,
        Boolean allowOfficial
) {

    public record UserFavorResponse(
            Boolean isPourSauceLover,
            Boolean isHardPeachLover,
            Boolean isMintChocoLover,
            Boolean isRedBeanFishBreadLover,
            Boolean isSojuLover,
            Boolean isRiceTteokLover
    ){}

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
