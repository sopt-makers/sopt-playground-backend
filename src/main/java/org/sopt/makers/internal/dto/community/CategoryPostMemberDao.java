package org.sopt.makers.internal.dto.community;

import com.querydsl.core.annotations.QueryProjection;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.community.Category;
import org.sopt.makers.internal.domain.community.CommunityPost;

public record CategoryPostMemberDao(
        CommunityPost posts,
        Member member,
        Category category
) {
    @QueryProjection
    public CategoryPostMemberDao {}
}
