package org.sopt.makers.internal.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record MemberProfileSpecificResponse(
        @Schema(required = true)
        String name,
        String profileImage,
        LocalDate birthday,
        Boolean isPhoneBlind,
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
        UserFavorResponse userFavor,
        String idealType,
        String selfIntroduction,
        @Schema(required = true)
        List<MemberActivityResponse> activities,
        @Schema(required = true)
        List<SoptMemberActivityResponse> soptActivities,
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

    public record UserFavorResponse(
            Boolean isPourSauceLover,
            Boolean isHardPeachLover,
            Boolean isMintChocoLover,
            Boolean isRedBeanFishBreadLover,
            Boolean isSojuLover,
            Boolean isRiceTteokLover
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

    public record MemberCardinalInfoResponse (
            String cardinalInfo
    ){}

    public record SoptMemberActivityResponse (
            Integer generation,
            String part,
            String team,
            List<MemberProjectVo> projects
    ){}

    public record MemberCareerResponse(
            Long id,
            String companyName,
            String title,
            String startDate,
            String endDate,
            Boolean isCurrent
    ){}

    public static MemberProfileSpecificResponse checkIsBlindPhone(MemberProfileSpecificResponse response, Boolean isMine) {
        return new MemberProfileSpecificResponse(
            response.name(),
            response.profileImage(),
            response.birthday(),
            response.isPhoneBlind(),
            response.isPhoneBlind() ? null : response.phone(),
            response.email(),
            response.address(),
            response.university(),
            response.major(),
            response.introduction(),
            response.skill(),
            response.mbti(),
            response.mbtiDescription(),
            response.sojuCapacity(),
            response.interest(),
            response.userFavor(),
            response.idealType(),
            response.selfIntroduction(),
            response.activities(),
            response.soptActivities(),
            response.links(),
            response.projects(),
            response.careers(),
            response.allowOfficial(),
            isMine
        );
    }
}
