package org.sopt.makers.internal.community.dto.response;

public record QuestionPostResponse(
        Long id,
        String title,
        String content,
        String createdAt,
        Integer likeCount,
        Integer commentCount,
        Boolean isAnswered
) {
}
