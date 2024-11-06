package org.sopt.makers.internal.member.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberInfoResponse(

        Long id,

        String name,

        Integer generation,

        String profileImage,

        Boolean hasProfile,

        Boolean editActivitiesAble,

        Boolean isCoffeeChatActivate
) {
}
