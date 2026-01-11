package org.sopt.makers.internal.external.pushNotification.message.member;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.sopt.makers.internal.external.pushNotification.message.PushNotificationMessageBuilder;

import static org.sopt.makers.internal.external.pushNotification.message.member.MemberPushConstants.*;

@RequiredArgsConstructor
public class AnswerNotificationMessage implements PushNotificationMessageBuilder {

	private final Long questionAskerId;
	private final String answerWriterName;
	private final String answerContent;
	private final String webLink;

	@Override
	public String buildTitle() {
		return ANSWER_NOTIFICATION_TITLE;
	}

	@Override
	public String buildContent() {
		String abbreviatedContent = StringUtils.abbreviate(answerContent, CONTENT_MAX_LENGTH);
		return String.format(ANSWER_CONTENT_FORMAT, answerWriterName, abbreviatedContent);
	}

	@Override
	public Long[] getRecipientIds() {
		return new Long[]{questionAskerId};
	}

	@Override
	public String getWebLink() {
		return webLink;
	}

	public static AnswerNotificationMessage of(
		Long questionAskerId,
		String answerWriterName,
		String answerContent,
		String webLink
	) {
		return new AnswerNotificationMessage(
			questionAskerId,
			answerWriterName,
			answerContent,
			webLink
		);
	}
}
