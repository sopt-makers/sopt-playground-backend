package org.sopt.makers.internal.member.controller.dto.response;

public record MemberInfoResponse(

        Long id,

        String name,

        Integer generation,

        String profileImage,

        Boolean hasProfile,

        Boolean editActivitiesAble,

        Boolean hasCoffeeChat
) {
}
