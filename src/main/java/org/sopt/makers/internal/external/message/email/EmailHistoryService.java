package org.sopt.makers.internal.external.message.email;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class EmailHistoryService {

	private final EmailHistoryRepository emailHistoryRepository;

	private final ZoneId KST = ZoneId.of("Asia/Seoul");

	public void createEmailHistory(CoffeeChatRequest request, Member sender, String email) {
		emailHistoryRepository.save(EmailHistory.builder()
				.senderId(sender.getId())
				.receiverId(request.receiverId())
				.senderEmail(email)
				.category(request.category().getTitle())
				.content(request.content())
				.createdAt(LocalDateTime.now(KST)).build());
	}
}
