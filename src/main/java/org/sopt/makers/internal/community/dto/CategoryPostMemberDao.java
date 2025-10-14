package org.sopt.makers.internal.community.dto;

import com.querydsl.core.annotations.QueryProjection;
import org.springframework.aot.hint.annotation.Reflective;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.domain.CommunityPost;

@Reflective
public record CategoryPostMemberDao(
        CommunityPost post,
        Member member,
        Category category
) {
    @QueryProjection
    public CategoryPostMemberDao {}
}
