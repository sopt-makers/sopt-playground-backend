package org.sopt.makers.internal.sopmodam.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SopmodamAnswerCreateRequest(
    @Schema(required = true, description = "답변 내용. 공백·줄바꿈 제외 20자 이상, 전체 500자 이하")
    @NotBlank(message = "답변 내용을 입력해주세요.")
    @Size(min = 20, max = 500, message = "답변 내용이 올바르지 않습니다.")
    String content
) {
}
