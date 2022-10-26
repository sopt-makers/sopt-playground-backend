package org.sopt.makers.internal.dto.member;

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
        String introduction,
        String skill,
        List<MemberLinkUpdateRequest> links,
        List<MemberSoptActivityUpdateRequest> activities,
        Boolean openToWork,
        Boolean openToSideProject,
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
            String category,
            String teamName
    ){}
}
