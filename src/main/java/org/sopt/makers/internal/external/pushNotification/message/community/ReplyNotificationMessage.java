package org.sopt.makers.internal.external.pushNotification.message.community;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.sopt.makers.internal.community.utils.MentionCleaner;
import org.sopt.makers.internal.external.pushNotification.message.PushNotificationMessageBuilder;

import static org.sopt.makers.internal.external.pushNotification.message.community.CommunityPushConstants.*;

@RequiredArgsConstructor
public class ReplyNotificationMessage implements PushNotificationMessageBuilder {

    private final Long parentCommentAuthorId;
    private final String replyWriterName;
    private final String replyContent;
    private final boolean isBlindWriter;
    private final String webLink;

    @Override
    public String buildTitle() {
        return REPLY_NOTIFICATION_TITLE;
    }

    @Override
    public String buildContent() {
        String writerName = isBlindWriter ? COMMENT_WRITER_ANONYMOUS : replyWriterName;
        String cleanedContent = MentionCleaner.removeMentionIds(replyContent);
        String abbreviatedContent = StringUtils.abbreviate(cleanedContent, CONTENT_MAX_LENGTH);
        return String.format(REPLY_CONTENT_FORMAT, writerName, abbreviatedContent);
    }

    @Override
    public Long[] getRecipientIds() {
        return new Long[]{parentCommentAuthorId};
    }

    @Override
    public String getWebLink() {
        return webLink;
    }

    public static ReplyNotificationMessage of(
            Long parentCommentAuthorId,
            String replyWriterName,
            String replyContent,
            boolean isBlindWriter,
            String webLink
    ) {
        return new ReplyNotificationMessage(
                parentCommentAuthorId,
                replyWriterName,
                replyContent,
                isBlindWriter,
                webLink
        );
    }
}
