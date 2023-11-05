package org.sopt.makers.internal.dto.community;

import com.querydsl.core.annotations.QueryProjection;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.community.CommunityPost;

public record CategoryPostMemberDao(
        Member member,
        CommunityPost posts
) {
    @QueryProjection
    public CategoryPostMemberDao {}
}
