package org.sopt.makers.internal.community.dto.response;

import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.dto.MemberVo;

public record PopularPostResponse(
        Long id,
        String category,
        String title,
        MemberVo member,
        Integer hits
) {
    public static PopularPostResponse of(CommunityPost post, String categoryName) {
        return new PopularPostResponse(
                post.getId(),
                categoryName,
                post.getTitle(),
                MemberVo.of(post.getMember()),
                post.getHits()
        );
    }
}