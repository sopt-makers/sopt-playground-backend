package org.sopt.makers.internal.dto.community;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;

public record PostDetailResponse(
        MemberVo member,
        @Schema(required = true)
        CommunityPostVo posts,
        @Schema(required = true)
        CategoryVo category,
        @Schema(required = true)
        Boolean isMine,
        @Schema(required = true)
        Boolean isLiked,
        @Schema(required = true)
        Integer likes
) {
    @QueryProjection
    public PostDetailResponse {}
}
