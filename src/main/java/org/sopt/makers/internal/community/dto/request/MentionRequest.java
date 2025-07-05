package org.sopt.makers.internal.community.dto.request;

public record MentionRequest(
        Long[] userIds,
        String writerName,
        String webLink
) {
}
