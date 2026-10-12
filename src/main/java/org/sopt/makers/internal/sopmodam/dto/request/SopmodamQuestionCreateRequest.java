package org.sopt.makers.internal.sopmodam.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SopmodamQuestionCreateRequest(
    @Schema(required = true, description = "추가할 투표 후보 질문. 255자 이하. 앞뒤 공백은 제거하고 저장", example = "첫 협업 전, 내 파트에서 무엇을 준비해두면 좋을까요?")
    @NotBlank(message = "필요한 값이 없습니다")
    @Size(max = 255, message = "질문은 255자 이하로 입력해야 합니다.")
    String content
) {
    // 어드민 화면과 같이 앞뒤 공백을 제거한 뒤 검증·저장한다
    public SopmodamQuestionCreateRequest {
        content = content == null ? null : content.strip();
    }
}
