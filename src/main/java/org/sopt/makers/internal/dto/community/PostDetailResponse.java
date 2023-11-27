package org.sopt.makers.internal.dto.community;

import com.querydsl.core.annotations.QueryProjection;

public record PostDetailResponse(
        MemberVo member,
        CommunityPostVo posts,
        CategoryVo category,
        Boolean isMine
) {
    @QueryProjection
    public PostDetailResponse {}
}
