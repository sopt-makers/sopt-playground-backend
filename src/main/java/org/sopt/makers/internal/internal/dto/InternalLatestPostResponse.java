package org.sopt.makers.internal.internal.dto;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.Builder;
import org.sopt.makers.internal.common.util.MentionCleaner;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousPostProfile;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
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
    public static InternalLatestPostResponse of(
            CommunityPost post,
            MemberSoptActivity latestActivity,
            String categoryName,
            Optional<AnonymousPostProfile> anonymousPostProfile
            ) {
        String generationAndPart = "";
        if (latestActivity != null) {
            generationAndPart = String.format("%d기 %s", latestActivity.getGeneration(), latestActivity.getPart());
        }

        String finalName = post.getMember().getName();
        String finalProfileImage = post.getMember().getProfileImage();

        if (post.getIsBlindWriter() && anonymousPostProfile.isPresent()) {
            AnonymousPostProfile anonymousProfile = anonymousPostProfile.get();
            finalName = anonymousProfile.getNickname().getNickname();
            finalProfileImage = anonymousProfile.getProfileImg().getImageUrl();
        }

        String cleanedContent = MentionCleaner.removeMentionIds(post.getContent());

        return new InternalLatestPostResponse(
                post.getId(),
                finalProfileImage,
                finalName,
                generationAndPart,
                categoryName,
                post.getTitle(),
                cleanedContent,
                "https://playground.sopt.org/?feed=" + post.getId(),
                post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"))
        );
    }
}
