package org.sopt.makers.internal.external;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.member.domain.coffeechat.ChatCategory;

public interface MessageSender {

	void sendMessage(Member sender, Member receiver, String content, String replyInfo, ChatCategory category);
}

