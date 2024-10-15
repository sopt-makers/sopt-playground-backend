package org.sopt.makers.internal.external;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.external.email.EmailChatSender;
import org.sopt.makers.internal.external.gabia.SmsChatSender;
import org.sopt.makers.internal.member.domain.coffeechat.ChatCategory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageSenderFactory {

	private final SmsChatSender smsChatSender;
	private final EmailChatSender emailChatSender;

	public MessageSender getSender(ChatCategory category) {
		return switch (category) {
			case COFFEE_CHAT -> smsChatSender;
			case FRIENDSHIP, APPJAM_TEAM_BUILD, PROJECT_PROPOSAL, OTHER -> emailChatSender;
		};
	}
}
