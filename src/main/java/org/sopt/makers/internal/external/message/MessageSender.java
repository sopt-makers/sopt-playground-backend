package org.sopt.makers.internal.external.message;

import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.coffeechat.domain.enums.ChatCategory;

public interface MessageSender {

	void sendMessage(InternalUserDetails sender, InternalUserDetails receiver, String content, String replyInfo, ChatCategory category);
}

