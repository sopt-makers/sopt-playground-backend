package org.sopt.makers.internal.community.dto.response;

import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.member.dto.response.MemberNameAndProfileImageResponse;

public record PopularPostResponse(
        Long id,
        String category,
        String title,
        MemberNameAndProfileImageResponse member,
        Integer hits
) {
    public static PopularPostResponse of(CommunityPost post, String categoryName) {
        return new PopularPostResponse(
                post.getId(),
                categoryName,
                post.getTitle(),
                MemberNameAndProfileImageResponse.from(post.getMember()),
                post.getHits()
        );
    }
}