package org.sopt.makers.internal.dto.member;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public record MemberProfileUpdateRequest (
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
        List<MemberLinkUpdateRequest> links,
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
