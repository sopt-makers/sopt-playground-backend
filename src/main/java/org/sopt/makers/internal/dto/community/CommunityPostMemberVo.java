package org.sopt.makers.internal.dto.community;

import com.querydsl.core.annotations.QueryProjection;

public record CommunityPostMemberVo(
        MemberVo member,
        CommunityPostVo post,
        CategoryVo category
) {
    @QueryProjection
    public CommunityPostMemberVo {}
}
