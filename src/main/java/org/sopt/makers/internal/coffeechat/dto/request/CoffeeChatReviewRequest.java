package org.sopt.makers.internal.coffeechat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
