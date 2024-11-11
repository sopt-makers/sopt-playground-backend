package org.sopt.makers.internal.member.controller.coffeechat.dto.response;

import java.util.List;

public record InternalCoffeeChatMemberResponse(
		Long id,
		List<String> parts,
		String name,
		String profileImageUrl
) {
}
