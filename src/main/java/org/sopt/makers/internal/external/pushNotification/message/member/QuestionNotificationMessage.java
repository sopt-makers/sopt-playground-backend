package org.sopt.makers.internal.external.pushNotification.message.member;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.sopt.makers.internal.external.pushNotification.message.PushNotificationMessageBuilder;

import static org.sopt.makers.internal.external.pushNotification.message.member.MemberPushConstants.*;

@RequiredArgsConstructor
public class QuestionNotificationMessage implements PushNotificationMessageBuilder {

	private final Long questionReceiverId;
	private final String questionContent;
	private final String webLink;

	@Override
	public String buildTitle() {
		return QUESTION_NOTIFICATION_TITLE;
	}

	@Override
	public String buildContent() {
		String abbreviatedContent = StringUtils.abbreviate(questionContent, CONTENT_MAX_LENGTH);
		return String.format(QUESTION_CONTENT_FORMAT, abbreviatedContent);
	}

	@Override
	public Long[] getRecipientIds() {
		return new Long[]{questionReceiverId};
	}

	@Override
	public String getWebLink() {
		return webLink;
	}

	public static QuestionNotificationMessage of(
		Long questionReceiverId,
		String questionContent,
		String webLink
	) {
		return new QuestionNotificationMessage(
			questionReceiverId,
			questionContent,
			webLink
		);
	}
}
