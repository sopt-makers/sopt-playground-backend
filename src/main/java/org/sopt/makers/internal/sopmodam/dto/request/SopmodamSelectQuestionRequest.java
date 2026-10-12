package org.sopt.makers.internal.sopmodam.dto.request;

import jakarta.validation.constraints.NotNull;

public record SopmodamSelectQuestionRequest(
    @NotNull(message = "questionId를 입력해주세요.")
    Long questionId
) {}
