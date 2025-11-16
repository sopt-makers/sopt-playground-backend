package org.sopt.makers.internal.external.message.gabia;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.coffeechat.domain.enums.ChatCategory;
import org.sopt.makers.internal.external.message.MessageSender;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.sms.SmsNotificationService;
import org.sopt.makers.internal.external.sms.message.coffeechat.CoffeeChatRequestSmsMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsChatSender implements MessageSender {

	private final SmsNotificationService smsNotificationService;
	private final PlatformService platformService;

	@Override
	public void sendMessage(
			InternalUserDetails sender,
			InternalUserDetails receiver,
			String content,
			String replyInfo,
			ChatCategory category
	) {
		String senderPart = String.join(", ", platformService.getPartAndGenerationList(sender.userId()));

		CoffeeChatRequestSmsMessage message = CoffeeChatRequestSmsMessage.of(
				sender.name(),
				senderPart,
				category,
				content,
				sender.userId(),
				replyInfo,
				receiver.phone()
		);

		smsNotificationService.sendSms(message);
	}
}
