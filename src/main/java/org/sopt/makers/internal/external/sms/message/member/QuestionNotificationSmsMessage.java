package org.sopt.makers.internal.external.sms.message.member;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.external.sms.message.SmsMessageBuilder;

@RequiredArgsConstructor
public class QuestionNotificationSmsMessage implements SmsMessageBuilder {

    private static final String MESSAGE_TITLE = "[SOPT makers] 내 에스크에 질문이 달렸어요!";
    private static final String CONTENT_LABEL = "- [이런 내용이 궁금해요] ";
    private static final String LINK_LABEL = "- [답변하러 가기] ";

    private final String questionContent;
    private final String askProfileLink;
    private final String receiverPhone;

    @Override
    public String buildMessage() {
        return MESSAGE_TITLE + "\n\n" +
                CONTENT_LABEL + questionContent + "\n" +
                LINK_LABEL + askProfileLink;
    }

    @Override
    public String getReceiverPhone() {
        return receiverPhone;
    }

    public static QuestionNotificationSmsMessage of(
            String questionContent,
            String askProfileLink,
            String receiverPhone
    ) {
        return new QuestionNotificationSmsMessage(
                questionContent,
                askProfileLink,
                receiverPhone
        );
    }
}
