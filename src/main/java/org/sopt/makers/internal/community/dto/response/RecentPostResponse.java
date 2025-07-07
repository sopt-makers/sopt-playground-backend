package org.sopt.makers.internal.community.dto.response;

public record RecentPostResponse(
        Long id,
        String title,
        String content,
        String createdAt,
        int likeCount,
        int commentCount,
        Long categoryId,
        String categoryName,
        Integer totalVoteCount,
        Boolean isAnswered
) {
}
