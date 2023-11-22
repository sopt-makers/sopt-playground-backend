package org.sopt.makers.internal.dto.community;

import com.querydsl.core.annotations.QueryProjection;

public record PostDetailResponse(
        CommunityPostMemberVo post,
        Boolean isMine
) {
    @QueryProjection
    public PostDetailResponse {}
}
