package org.sopt.makers.internal.external.makers;

import java.time.LocalDateTime;

public record CrewPost(
        long id,
        String title,
        String contents,
        LocalDateTime createdDate,
        String[] images,
        CrewUser user,
        int likeCount,
        boolean isLiked,
        int viewCount,
        int commentCount,
        Long meetingId
) {
    public record CrewUser(
            long id,
            long orgId,
            String name,
            String profileImage,
            PartInfo partInfo
    ) {}

    public record PartInfo(
            String part,
            int generation
    ) {}
}
