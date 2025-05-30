package org.sopt.makers.internal.member.dto.response;

import java.util.List;

public record MakersMemberProfileResponse (
        Long id,
        String name,
        String profileImage,
        List<MakersMemberProfileResponse.MemberSoptActivityResponse> activities,
        List<MakersMemberProfileResponse.MemberCareerResponse> careers
){

    public record MemberSoptActivityResponse(
            Long id,
            Integer generation
    ){}

    public record MemberCareerResponse(
            Long id,
            String companyName,
            String title,
            Boolean isCurrent
    ){}
}
