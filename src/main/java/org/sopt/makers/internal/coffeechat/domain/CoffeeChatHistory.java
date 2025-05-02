package org.sopt.makers.internal.coffeechat.domain;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.common.AuditingTimeEntity;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoffeeChatHistory extends AuditingTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver_id", nullable = false)
	private Member receiver;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id", nullable = false)
	private Member sender;

	private String requestContent;

	@Builder
	private CoffeeChatHistory(Member receiver, Member sender, String requestContent) {
		this.receiver = receiver;
		this.sender = sender;
		this.requestContent = requestContent;
	}
}
