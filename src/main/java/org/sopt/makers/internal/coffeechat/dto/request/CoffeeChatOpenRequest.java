package org.sopt.makers.internal.coffeechat.dto.request;

import jakarta.validation.constraints.NotNull;

public record CoffeeChatOpenRequest(
		@NotNull(message = "공개 여부는 필수 입력 값입니다.")
		Boolean open
) {
}
