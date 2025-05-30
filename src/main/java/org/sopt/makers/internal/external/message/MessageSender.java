package org.sopt.makers.internal.external.message;

import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.coffeechat.domain.enums.ChatCategory;

public interface MessageSender {

	void sendMessage(Member sender, Member receiver, String content, String replyInfo, ChatCategory category);
}

