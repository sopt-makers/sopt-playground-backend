package org.sopt.makers.internal.external.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.exception.BusinessLogicException;
import org.sopt.makers.internal.external.MessageSender;
import org.sopt.makers.internal.coffeechat.domain.enums.ChatCategory;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailChatSender implements MessageSender {

	private final EmailSender emailSender;

	@Override
	public void sendMessage(Member sender, Member receiver, String content, String replyInfo, ChatCategory category) {
		String html = emailSender.createCoffeeChatEmailHtml(
				sender.getName(),
				replyInfo,
				sender.getId(),
				category.getTitle(),
				sender.getProfileImage(),
				content
		);
		String subject = """
            띠링, %s님이 보낸 쪽지가 도착했어요!""".formatted(sender.getName());
		try {
			emailSender.sendEmail(
					receiver.getEmail(),
					subject,
					html
			);
		} catch (MessagingException | UnsupportedEncodingException exception) {
			log.error(exception.getMessage());
			throw new BusinessLogicException("커피챗 이메일 전송에 실패했습니다.");
		}
	}
}
