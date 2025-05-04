package org.sopt.makers.internal.community.dto;

import com.querydsl.core.annotations.QueryProjection;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.domain.CommunityPost;

public record CategoryPostMemberDao(
        CommunityPost posts,
        Member member,
        Category category
) {
    @QueryProjection
    public CategoryPostMemberDao {}
}
