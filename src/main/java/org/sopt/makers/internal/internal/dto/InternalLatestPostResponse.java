package org.sopt.makers.internal.internal.dto;

import java.time.format.DateTimeFormatter;
import lombok.Builder;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.member.domain.MemberSoptActivity;

@Builder
public record InternalLatestPostResponse(
        Long id,
        String profileImage,
        String name,
        String generationAndPart,
        String category,
        String title,
        String content,
        String webLink,
        String createdAt
) {
    public static InternalLatestPostResponse of(CommunityPost post, MemberSoptActivity latestActivity, String categoryName) {
        String generationAndPart = "";
        if (latestActivity != null) {
            generationAndPart = String.format("%d기 %s", latestActivity.getGeneration(), latestActivity.getPart());
        }

        return new InternalLatestPostResponse(
                post.getId(),
                post.getMember().getProfileImage(),
                post.getMember().getName(),
                generationAndPart,
                categoryName,
                post.getTitle(),
                post.getContent(),
                "https://playground.sopt.org/?feed=" + post.getId(),
                post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"))
        );
    }
}
