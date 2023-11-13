package org.sopt.makers.internal.dto.community;

import com.querydsl.core.annotations.QueryProjection;
import org.sopt.makers.internal.domain.community.CommunityPost;

public record CommunityPostMemberVo(
        MemberVo member,
        CommunityPost posts,
        CategoryVo category
) {
    @QueryProjection
    public CommunityPostMemberVo {}
}
