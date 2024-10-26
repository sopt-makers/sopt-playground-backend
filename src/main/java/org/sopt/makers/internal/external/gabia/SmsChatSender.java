package org.sopt.makers.internal.external.gabia;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.external.MessageSender;
import org.sopt.makers.internal.member.domain.coffeechat.ChatCategory;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsChatSender implements MessageSender {

	private final GabiaService gabiaService;
	private final MemberRetriever memberRetriever;

	@Override
	public void sendMessage(Member sender, Member receiver, String content, String replyInfo, ChatCategory category) {
		String message = "[SOPT makers] 커피챗 제안이 도착했어요!\n" +
				"연결을 원하신다면 멤버의 전화번호로 직접 연락해 주세요.\n\n" +
				"- 이름: " + sender.getName() + "\n" +
				"- 연락처: " + replyInfo + "\n" +
				"- 파트: " + memberRetriever.concatPartAndGeneration(sender.getId()) + "\n" +
				"- 멤버 프로필 링크: https://playground.sopt.org/members/" + sender.getId() + "\n\n" +
				"- 이런 내용이 궁금해요\n" +
				content;

		gabiaService.sendSMS(receiver.getPhone(), message);
	}
}