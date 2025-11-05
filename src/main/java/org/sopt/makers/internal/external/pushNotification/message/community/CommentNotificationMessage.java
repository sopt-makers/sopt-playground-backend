package org.sopt.makers.internal.external.pushNotification.message.community;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.sopt.makers.internal.community.utils.MentionCleaner;
import org.sopt.makers.internal.external.pushNotification.message.PushNotificationMessageBuilder;

import static org.sopt.makers.internal.external.pushNotification.message.community.CommunityPushConstants.*;

@RequiredArgsConstructor
public class CommentNotificationMessage implements PushNotificationMessageBuilder {

    private final Long postAuthorId;
    private final String commentWriterName;
    private final String commentContent;
    private final boolean isBlindWriter;
    private final String webLink;

    @Override
    public String buildTitle() {
        return COMMENT_NOTIFICATION_TITLE;
    }

    @Override
    public String buildContent() {
        String writerName = isBlindWriter ? COMMENT_WRITER_ANONYMOUS : commentWriterName;
        String cleanedContent = MentionCleaner.removeMentionIds(commentContent);
        String abbreviatedContent = StringUtils.abbreviate(cleanedContent, CONTENT_MAX_LENGTH);
        return String.format(COMMENT_CONTENT_FORMAT, writerName, abbreviatedContent);
    }

    @Override
    public Long[] getRecipientIds() {
        return new Long[]{postAuthorId};
    }

    @Override
    public String getWebLink() {
        return webLink;
    }

    public static CommentNotificationMessage of(
            Long postAuthorId,
            String commentWriterName,
            String commentContent,
            boolean isBlindWriter,
            String webLink
    ) {
        return new CommentNotificationMessage(
                postAuthorId,
                commentWriterName,
                commentContent,
                isBlindWriter,
                webLink
        );
    }
}
