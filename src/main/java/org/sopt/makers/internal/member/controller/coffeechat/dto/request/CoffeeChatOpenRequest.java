package org.sopt.makers.internal.member.controller.coffeechat.dto.request;

import javax.validation.constraints.NotNull;

public record CoffeeChatOpenRequest(
		@NotNull(message = "공개 여부는 필수 입력 값입니다.")
		Boolean open
) {
}
