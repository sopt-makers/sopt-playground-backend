package org.sopt.makers.internal.external.message.gabia;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.message.MessageSender;
import org.sopt.makers.internal.coffeechat.domain.enums.ChatCategory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsChatSender implements MessageSender {

	private final GabiaService gabiaService;
	private final PlatformService platformService;

	@Override
	public void sendMessage(InternalUserDetails sender, InternalUserDetails receiver, String content, String replyInfo, ChatCategory category) {
		String message = "[SOPT makers] 커피챗 제안이 도착했어요!\n" +
				"연결을 원하신다면 멤버의 전화번호로 직접 연락해 주세요.\n\n" +
				"- 이름: " + sender.name() + "\n" +
				"- 연락처: " + replyInfo + "\n" +
				"- 파트: " + String.join(", ", platformService.getPartAndGenerationList(sender.userId())) + "\n" +
				"- 멤버 프로필 링크: https://playground.sopt.org/members/" + sender.userId() + "\n\n" +
				"- 이런 내용이 궁금해요\n" +
				content;

		gabiaService.sendSMS(receiver.phone(), message);
	}
}
