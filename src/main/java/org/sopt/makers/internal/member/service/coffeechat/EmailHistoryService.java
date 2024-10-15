package org.sopt.makers.internal.member.service.coffeechat;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.EmailHistory;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.member.dto.request.CoffeeChatRequest;
import org.sopt.makers.internal.repository.EmailHistoryRepository;
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
				.category(request.category().getValue())
				.content(request.content())
				.createdAt(LocalDateTime.now(KST)).build());
	}
}
