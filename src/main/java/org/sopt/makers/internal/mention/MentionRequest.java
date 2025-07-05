package org.sopt.makers.internal.mention;

public record MentionRequest(
        Long[] userIds,
        String writerName,
        String webLink
) {
}
