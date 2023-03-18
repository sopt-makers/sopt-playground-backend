package org.sopt.makers.internal.dto.member;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record MemberProfileUpdateRequest (
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
        List<MemberLinkUpdateRequest> links,
        @Schema(required = true)
        List<MemberSoptActivityUpdateRequest> activities,
        List<MemberCareerUpdateRequest> careers,
        Boolean allowOfficial
){

    public record MemberLinkUpdateRequest(
            Long id,
            String title,
            String url
    ){}

    public record MemberSoptActivityUpdateRequest(
            Long id,
            Integer generation,
            String part,
            String team
    ){}

    public record MemberCareerUpdateRequest(
            String companyName,
            String title,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
            String startDate,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
            String endDate,
            Boolean isCurrent
    ){}
}
