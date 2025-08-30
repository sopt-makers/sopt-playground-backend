package org.sopt.makers.internal.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record MemberProfileResponse(
    @Schema(required = true)
    Long id,
    @Schema(required = true)
    String name,
    String profileImage,
    String birthday,
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
    List<MemberSoptActivityResponse> activities,
    List<MemberLinkResponse> links,
    List<MemberCareerResponse> careers,
    Boolean allowOfficial,
    Boolean isCoffeeChatActivate
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

    public static MemberProfileResponse checkIsBlindPhone(MemberProfileResponse response, String phone) {
        return new MemberProfileResponse(
            response.id(),
            response.name(),
            response.profileImage(),
            response.birthday(),
            phone,
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
            response.links(),
            response.careers(),
            response.allowOfficial(),
            response.isCoffeeChatActivate()
        );
    }

}
