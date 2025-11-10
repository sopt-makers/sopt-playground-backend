package org.sopt.makers.internal.external.sms.message.coffeechat;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.coffeechat.domain.enums.ChatCategory;
import org.sopt.makers.internal.external.sms.message.SmsMessageBuilder;

import static org.sopt.makers.internal.external.sms.message.coffeechat.CoffeeChatSmsConstants.*;

@RequiredArgsConstructor
public class CoffeeChatRequestSmsMessage implements SmsMessageBuilder {

    private final String senderName;
    private final String senderPart;
    private final String topic;
    private final String content;
    private final Long senderId;
    private final String senderPhone;
    private final String receiverPhone;

    @Override
    public String buildMessage() {
        StringBuilder message = new StringBuilder();

        // 헤더
        message.append(WEB_SENDER).append("\n");
        message.append(MESSAGE_TITLE).append("\n\n");

        // 발신자 정보
        message.append(NAME_LABEL).append(senderName).append("\n");
        message.append(PART_LABEL).append(senderPart).append("\n");
        message.append(TOPIC_LABEL).append(topic).append("\n");

        // 내용
        message.append(CONTENT_LABEL);
        message.append(content).append("\n\n");

        // 프로필 링크
        message.append(PROFILE_LINK_LABEL);
        message.append(String.format(PROFILE_URL_FORMAT, senderId)).append("\n\n");

        // 안내 메시지
        message.append(GUIDE_MESSAGE).append("\n\n");

        // 연락처
        message.append(PHONE_LABEL).append(senderPhone);

        return message.toString();
    }

    @Override
    public String getReceiverPhone() {
        return receiverPhone;
    }

    public static CoffeeChatRequestSmsMessage of(
            String senderName,
            String senderPart,
            ChatCategory category,
            String content,
            Long senderId,
            String senderPhone,
            String receiverPhone
    ) {
        String topic = category == ChatCategory.COFFEE_CHAT ? TOPIC_COFFEE_CHAT : TOPIC_NOTE;

        return new CoffeeChatRequestSmsMessage(
                senderName,
                senderPart,
                topic,
                content,
                senderId,
                senderPhone,
                receiverPhone
        );
    }
}
