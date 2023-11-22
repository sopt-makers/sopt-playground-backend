package org.sopt.makers.internal.dto.community;

import com.querydsl.core.annotations.QueryProjection;
import org.sopt.makers.internal.domain.community.CommunityPost;

public record PostDetailResponse(
        MemberVo member,
        CommunityPost posts,
        CategoryVo category,
        Boolean isMine
) {
    @QueryProjection
    public PostDetailResponse {}
}
