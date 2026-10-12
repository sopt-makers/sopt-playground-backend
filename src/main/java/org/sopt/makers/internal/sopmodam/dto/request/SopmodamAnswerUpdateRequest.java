package org.sopt.makers.internal.sopmodam.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SopmodamAnswerUpdateRequest(
    @Schema(required = true, description = "수정할 답변 내용. 1자 이상 500자 이하, 공백만으로 이루어진 경우 불가")
    @NotBlank(message = "답변 내용을 입력해주세요.")
    @Size(max = 500, message = "답변 내용이 올바르지 않습니다.")
    String content
) {
}
