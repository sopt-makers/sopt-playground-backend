package org.sopt.makers.internal.member.dto.request;

import javax.validation.constraints.NotBlank;

public record CoffeeChatOpenRequest(
		@NotBlank(message = "공개 여부는 필수 입력 값입니다.")
		Boolean open
) {
}
