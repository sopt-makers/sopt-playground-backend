package org.sopt.makers.internal.external.pushNotification.message.community;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.sopt.makers.internal.common.util.MentionCleaner;
import org.sopt.makers.internal.external.pushNotification.message.PushNotificationMessageBuilder;

import static org.sopt.makers.internal.external.pushNotification.message.community.CommunityPushConstants.*;

@RequiredArgsConstructor
public class MentionNotificationMessage implements PushNotificationMessageBuilder {

    private final Long[] mentionedUserIds;
    private final String mentionerName;
    private final String commentContent;
    private final boolean isBlindWriter;
    private final String webLink;

    @Override
    public String buildTitle() {
        String writerName = isBlindWriter ? COMMENT_WRITER_ANONYMOUS : mentionerName;
        return String.format(MENTION_NOTIFICATION_TITLE_FORMAT, writerName);
    }

    @Override
    public String buildContent() {
        String cleanedContent = MentionCleaner.removeMentionIds(commentContent);
        String abbreviatedContent = StringUtils.abbreviate(cleanedContent, CONTENT_MAX_LENGTH);
        return String.format(MENTION_CONTENT_FORMAT, abbreviatedContent);
    }

    @Override
    public Long[] getRecipientIds() {
        return mentionedUserIds;
    }

    @Override
    public String getWebLink() {
        return webLink;
    }

    public static MentionNotificationMessage of(
            Long[] mentionedUserIds,
            String mentionerName,
            String commentContent,
            boolean isBlindWriter,
            String webLink
    ) {
        return new MentionNotificationMessage(
                mentionedUserIds,
                mentionerName,
                commentContent,
                isBlindWriter,
                webLink
        );
    }
}
