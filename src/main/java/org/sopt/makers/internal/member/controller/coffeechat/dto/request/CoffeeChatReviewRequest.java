package org.sopt.makers.internal.member.controller.coffeechat.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record CoffeeChatReviewRequest(

        @NotNull
        Long coffeeChatId,

        @NotBlank
        @Size(min = 1, max = 10)
        String nickname,

        @NotBlank
        String content
) {
}
