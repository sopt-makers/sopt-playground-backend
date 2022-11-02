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
        Map<CardinalVo, List<ActivityVo>> activities,
        List<MemberLinkResponse> links,
        List<MemberProjectResponse> projects,
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
            String thumbnailImage
    ){}
}
