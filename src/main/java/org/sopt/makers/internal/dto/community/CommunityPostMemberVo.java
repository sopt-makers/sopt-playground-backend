package org.sopt.makers.internal.dto.community;

import com.querydsl.core.annotations.QueryProjection;
import org.sopt.makers.internal.domain.community.CommunityPost;

public record CommunityPostMemberVo(
        MemberVo member,
        CommunityPostVo post,
        CategoryVo category
) {
    @QueryProjection
    public CommunityPostMemberVo {}
}
