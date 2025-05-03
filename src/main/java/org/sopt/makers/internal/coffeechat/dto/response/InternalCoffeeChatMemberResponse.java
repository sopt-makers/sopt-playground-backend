package org.sopt.makers.internal.coffeechat.dto.response;

import java.util.List;

public record InternalCoffeeChatMemberResponse(
		Long id,
		List<String> parts,
		String name,
		String profileImageUrl
) {
}
