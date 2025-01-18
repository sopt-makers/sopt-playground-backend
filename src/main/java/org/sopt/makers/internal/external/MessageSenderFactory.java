package org.sopt.makers.internal.external;

import lombok.RequiredArgsConstructor;

import org.sopt.makers.internal.external.email.EmailChatSender;
import org.sopt.makers.internal.external.gabia.SmsChatSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageSenderFactory {

	private final SmsChatSender smsChatSender;
	private final EmailChatSender emailChatSender;

	public MessageSender getSender(String senderEmail, String senderPhone) {
		if (senderEmail != null) {
			return emailChatSender;
		}
		if (senderPhone != null) {
			return smsChatSender;
		}
		return null;
	}
}
