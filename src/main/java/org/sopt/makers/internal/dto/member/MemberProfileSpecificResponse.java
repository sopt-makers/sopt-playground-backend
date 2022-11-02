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
        String introduction,
        String skill,
        List<MemberSoptActivityResponse> activities,
        List<MemberLinkResponse> links,
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

    public record MemberSoptActivityResponse(
            Long id,
            Integer generation,
            String part,
            String category,
            String teamName
    ){}
}
