package org.sopt.makers.internal.external.message.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.external.message.MessageSender;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.exception.BusinessLogicException;
import org.sopt.makers.internal.coffeechat.domain.enums.ChatCategory;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailChatSender implements MessageSender {

	private final EmailSender emailSender;

	@Override
	public void sendMessage(InternalUserDetails sender, InternalUserDetails receiver, String content, String replyInfo, ChatCategory category) {
		String html = emailSender.createCoffeeChatEmailHtml(
				sender.name(),
				replyInfo,
				sender.userId(),
				category.getTitle(),
				sender.profileImage(),
				content
		);
		String subject = """
            띠링, %s님이 보낸 쪽지가 도착했어요!""".formatted(sender.name());
		try {
			emailSender.sendEmail(
					receiver.email(),
					subject,
					html
			);
		} catch (MessagingException | UnsupportedEncodingException exception) {
			log.error(exception.getMessage());
			throw new BusinessLogicException("커피챗 이메일 전송에 실패했습니다.");
		}
	}
}
