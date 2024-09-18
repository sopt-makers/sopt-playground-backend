package org.sopt.makers.internal.community.controller.dto.response;

import java.util.List;

public record CommunityCategoryResponse(

        Long id,

        String name,

        String content,

        Boolean hasAll,

        Boolean hasBlind,

        Boolean hasQuestion,

        List<CommunityCategoryResponse> children
) {
}
